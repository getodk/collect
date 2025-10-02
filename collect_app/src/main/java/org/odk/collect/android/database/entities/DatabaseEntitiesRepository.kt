package org.odk.collect.android.database.entities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.provider.BaseColumns._ID
import org.odk.collect.db.sqlite.CursorExt.first
import org.odk.collect.db.sqlite.CursorExt.foldAndClose
import org.odk.collect.db.sqlite.CursorExt.getBoolean
import org.odk.collect.db.sqlite.CursorExt.getInt
import org.odk.collect.db.sqlite.CursorExt.getLongOrNull
import org.odk.collect.db.sqlite.CursorExt.getString
import org.odk.collect.db.sqlite.CursorExt.getStringOrNull
import org.odk.collect.db.sqlite.CursorExt.rowToMap
import org.odk.collect.db.sqlite.MigrationListDatabaseMigrator
import org.odk.collect.db.sqlite.RowNumbers.invalidateRowNumbers
import org.odk.collect.db.sqlite.RowNumbers.rawQueryWithRowNumber
import org.odk.collect.db.sqlite.SQLiteColumns.ROW_NUMBER
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.addColumn
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.getColumnNames
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.query
import org.odk.collect.db.sqlite.SynchronizedDatabaseConnection
import org.odk.collect.db.sqlite.toSql
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.EntityList
import org.odk.collect.entities.storage.QueryException
import org.odk.collect.entities.storage.getListNames
import org.odk.collect.shared.Query
import org.odk.collect.shared.mapColumns

private object ListsTable {
    const val TABLE_NAME = "lists"
    const val COLUMN_NAME = "name"
    const val COLUMN_HASH = "hash"
    const val COLUMN_NEEDS_APPROVAL = "needs_approval"
    const val COLUMN_LAST_UPDATED = "last_updated"
}

private object EntitiesTable {
    const val COLUMN_ID = "id"
    const val COLUMN_LABEL = "label"
    const val COLUMN_VERSION = "version"
    const val COLUMN_TRUNK_VERSION = "trunk_version"
    const val COLUMN_BRANCH_ID = "branch_id"
    const val COLUMN_STATE = "state"
    const val COLUMN_PROPERTY_PREFIX = "p_"

    fun getPropertyColumn(property: String) = "$COLUMN_PROPERTY_PREFIX$property"
}

class DatabaseEntitiesRepository(context: Context, dbPath: String, private val clock: () -> Long) :
    EntitiesRepository {

    private val databaseConnection = SynchronizedDatabaseConnection(
        context,
        dbPath,
        "entities.db",
        EntitiesDatabaseMigrator(DATABASE_VERSION),
        DATABASE_VERSION
    )

    override fun save(list: String, vararg entities: Entity) {
        if (entities.isEmpty()) {
            return
        }

        val listExists = listExists(list)
        if (!listExists) {
            createList(list)
        }

        updatePropertyColumns(list, entities.first())

        databaseConnection.transaction {
            entities.forEach { entity ->
                val existing = if (listExists) {
                    query(
                        quote(list),
                        "${EntitiesTable.COLUMN_ID} = ?",
                        arrayOf(entity.id)
                    ).first { mapCursorRowToEntity(it, 0) }
                } else {
                    null
                }

                if (existing != null) {
                    val state = if (existing.state == Entity.State.OFFLINE) {
                        entity.state
                    } else {
                        Entity.State.ONLINE
                    }

                    val contentValues = ContentValues().also {
                        it.put(EntitiesTable.COLUMN_ID, entity.id)
                        it.put(EntitiesTable.COLUMN_LABEL, entity.label ?: existing.label)
                        it.put(EntitiesTable.COLUMN_VERSION, entity.version)
                        it.put(EntitiesTable.COLUMN_TRUNK_VERSION, entity.trunkVersion)
                        it.put(EntitiesTable.COLUMN_BRANCH_ID, entity.branchId)
                        it.put(EntitiesTable.COLUMN_STATE, convertStateToInt(state))

                        addPropertiesToContentValues(it, entity)
                    }

                    update(
                        quote(list),
                        contentValues,
                        "${EntitiesTable.COLUMN_ID} = ?",
                        arrayOf(entity.id)
                    )
                } else {
                    val contentValues = ContentValues().also {
                        it.put(EntitiesTable.COLUMN_ID, entity.id)
                        it.put(EntitiesTable.COLUMN_LABEL, entity.label)
                        it.put(EntitiesTable.COLUMN_VERSION, entity.version)
                        it.put(EntitiesTable.COLUMN_TRUNK_VERSION, entity.trunkVersion)
                        it.put(EntitiesTable.COLUMN_BRANCH_ID, entity.branchId)
                        it.put(EntitiesTable.COLUMN_STATE, convertStateToInt(entity.state))

                        addPropertiesToContentValues(it, entity)
                    }

                    insertOrThrow(
                        quote(list),
                        null,
                        contentValues
                    )
                }
            }
        }

        invalidateRowNumbers()
    }

    override fun getLists(): List<EntityList> {
        return databaseConnection.withConnection {
            readableDatabase
                .query(ListsTable.TABLE_NAME)
                .foldAndClose(emptyList()) { list, cursor ->
                    list + mapCursorRowToEntityList(cursor)
                }
        }
    }

    override fun updateList(list: String, hash: String, needsApproval: Boolean) {
        if (!listExists(list)) {
            createList(list)
        }

        val contentValues = ContentValues().also {
            it.put(ListsTable.COLUMN_NAME, list)
            it.put(ListsTable.COLUMN_HASH, hash)
            it.put(ListsTable.COLUMN_NEEDS_APPROVAL, if (needsApproval) 1 else 0)
            it.put(ListsTable.COLUMN_LAST_UPDATED, clock())
        }

        databaseConnection.withConnection {
            writableDatabase.update(
                ListsTable.TABLE_NAME,
                contentValues,
                "${ListsTable.COLUMN_NAME} = ?",
                arrayOf(list)
            )
        }
    }

    override fun getList(list: String): EntityList? {
        return databaseConnection.withConnection {
            readableDatabase
                .query(ListsTable.TABLE_NAME, "${ListsTable.COLUMN_NAME} = ?", arrayOf(list))
                .first { mapCursorRowToEntityList(it) }
        }
    }

    override fun getCount(list: String): Int {
        if (!listExists(list)) {
            return 0
        }

        return databaseConnection.withConnection {
            readableDatabase.rawQuery(
                """
                SELECT COUNT(*)
                FROM "$list"
                """.trimIndent(),
                null
            ).first {
                it.getInt(0)
            }!!
        }
    }

    override fun addList(list: String) {
        if (!listExists(list)) {
            createList(list)
            invalidateRowNumbers()
        }
    }

    override fun delete(list: String, id: String) {
        databaseConnection.withConnection {
            writableDatabase.delete(quote(list), "${EntitiesTable.COLUMN_ID} = ?", arrayOf(id))
        }

        invalidateRowNumbers()
    }

    override fun query(list: String, query: Query?): List<Entity.Saved> {
        if (!listExists(list)) {
            return emptyList()
        }

        return queryWithAttachedRowNumber(list, query?.mapColumns { columnName ->
            when (columnName) {
                EntitySchema.ID -> EntitiesTable.COLUMN_ID
                EntitySchema.LABEL -> EntitiesTable.COLUMN_LABEL
                EntitySchema.VERSION -> EntitiesTable.COLUMN_VERSION
                else -> EntitiesTable.getPropertyColumn(columnName)
            }
        })
    }

    override fun getByIndex(list: String, index: Int): Entity.Saved? {
        if (!listExists(list)) {
            return null
        }

        val query = Query.StringEq(ROW_NUMBER, (index + 1).toString())
        return queryWithAttachedRowNumber(list, query).firstOrNull()
    }

    private fun queryWithAttachedRowNumber(list: String, query: Query?): List<Entity.Saved> {
        try {
            val cursorMapper = { cursor: Cursor ->
                cursor.foldAndClose {
                    mapCursorRowToEntity(it, it.getInt(ROW_NUMBER))
                }
            }

            return if (query == null) {
                databaseConnection.rawQueryWithRowNumber(list, cursorMapper = cursorMapper)
            } else {
                val sqlQuery = query.toSql()
                databaseConnection.rawQueryWithRowNumber(
                    list,
                    sqlQuery.selection,
                    sqlQuery.selectionArgs,
                    cursorMapper = cursorMapper
                )
            }
        } catch (e: SQLiteException) {
            throw QueryException(e.message)
        }
    }

    private fun invalidateRowNumbers() {
        getListNames().forEach {
            databaseConnection.invalidateRowNumbers(it)
        }
    }

    private fun listExists(list: String): Boolean {
        return databaseConnection.withConnection {
            readableDatabase
                .query(
                    ListsTable.TABLE_NAME,
                    selection = "${ListsTable.COLUMN_NAME} = ?",
                    selectionArgs = arrayOf(list)
                ).use { it.count } > 0
        }
    }

    private fun createList(list: String) {
        databaseConnection.resetTransaction {
            val contentValues = ContentValues()
            contentValues.put(ListsTable.COLUMN_NAME, list)
            insertOrThrow(
                ListsTable.TABLE_NAME,
                null,
                contentValues
            )

            execSQL(
                """
                CREATE TABLE IF NOT EXISTS "$list" (
                    $_ID integer PRIMARY KEY,
                    ${EntitiesTable.COLUMN_ID} text,
                    ${EntitiesTable.COLUMN_LABEL} text,
                    ${EntitiesTable.COLUMN_VERSION} integer,
                    ${EntitiesTable.COLUMN_TRUNK_VERSION} integer,
                    ${EntitiesTable.COLUMN_BRANCH_ID} text,
                    ${EntitiesTable.COLUMN_STATE} integer NOT NULL
                );
                """.trimIndent()
            )

            execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS "${list}_unique_id_index" ON "$list" (${EntitiesTable.COLUMN_ID});
                """.trimIndent()
            )
        }
    }

    private fun updatePropertyColumns(list: String, entity: Entity) {
        val columnNames = databaseConnection.withConnection {
            readableDatabase.getColumnNames(quote(list))
        }

        val missingColumns = entity.properties
            .map { EntitiesTable.getPropertyColumn(it.first) }
            .distinctBy { it.lowercase() }
            .filterNot { columnName ->
                columnNames.any {
                    it.equals(
                        columnName,
                        ignoreCase = true
                    )
                }
            }

        if (missingColumns.isNotEmpty()) {
            databaseConnection.resetTransaction {
                missingColumns.forEach {
                    execSQL(
                        """
                        ALTER TABLE "$list" ADD "$it" text NOT NULL DEFAULT "";
                        """.trimIndent()
                    )
                }
            }
        }
    }

    private fun addPropertiesToContentValues(contentValues: ContentValues, entity: Entity) {
        entity.properties.forEach { (name, value) ->
            contentValues.put(quote(EntitiesTable.getPropertyColumn(name)), value)
        }
    }

    private fun mapCursorRowToEntityList(cursor: Cursor): EntityList {
        return EntityList(
            cursor.getString(ListsTable.COLUMN_NAME),
            cursor.getStringOrNull(ListsTable.COLUMN_HASH),
            cursor.getBoolean(ListsTable.COLUMN_NEEDS_APPROVAL),
            cursor.getLongOrNull(ListsTable.COLUMN_LAST_UPDATED)
        )
    }

    private fun mapCursorRowToEntity(
        cursor: Cursor,
        rowId: Int
    ): Entity.Saved {
        val map = cursor.rowToMap()

        val propertyColumns = map.keys.filter {
            it.startsWith(EntitiesTable.COLUMN_PROPERTY_PREFIX)
        }

        val properties =
            propertyColumns.fold(emptyList<Pair<String, String>>()) { accum, property ->
                accum + Pair(
                    property.removePrefix(EntitiesTable.COLUMN_PROPERTY_PREFIX),
                    map[property] ?: ""
                )
            }

        val state = if (map[EntitiesTable.COLUMN_STATE]!!.toInt() == 0) {
            Entity.State.OFFLINE
        } else {
            Entity.State.ONLINE
        }

        return Entity.Saved(
            map[EntitiesTable.COLUMN_ID]!!,
            map[EntitiesTable.COLUMN_LABEL],
            map[EntitiesTable.COLUMN_VERSION]!!.toInt(),
            properties,
            state,
            rowId - 1,
            map[EntitiesTable.COLUMN_TRUNK_VERSION]?.toInt(),
            map[EntitiesTable.COLUMN_BRANCH_ID]!!
        )
    }

    /**
     * Store state as an Int rather than a string to avoid increasing the storage needed for
     * entities.
     */
    private fun convertStateToInt(state: Entity.State): Int {
        return when (state) {
            Entity.State.OFFLINE -> 0
            Entity.State.ONLINE -> 1
        }
    }

    private fun quote(text: String) = "\"$text\""

    companion object {
        const val DATABASE_VERSION = 4
    }
}

class EntitiesDatabaseMigrator(databaseVersion: Int) : MigrationListDatabaseMigrator(
    databaseVersion,
    {
        throw IllegalStateException("Cannot upgrade from this beta version. Please reinstall Collect!")
    },
    {
        it.addColumn(
            ListsTable.TABLE_NAME,
            ListsTable.COLUMN_NEEDS_APPROVAL,
            "integer",
            default = "0"
        )
    },
    {
        it.addColumn(
            ListsTable.TABLE_NAME,
            ListsTable.COLUMN_LAST_UPDATED,
            "date"
        )
    }
) {
    override fun createDbForVersion(db: SQLiteDatabase, version: Int) {
        if (version == 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lists (
                        _id integer PRIMARY KEY,
                        name text NOT NULL,
                        hash text
                );
                """.trimIndent()
            )
        } else if (version == 3) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ${ListsTable.TABLE_NAME} (
                        $_ID integer PRIMARY KEY,
                        ${ListsTable.COLUMN_NAME} text NOT NULL,
                        ${ListsTable.COLUMN_HASH} text,
                        ${ListsTable.COLUMN_NEEDS_APPROVAL} integer DEFAULT 0
                );
                """.trimIndent()
            )
        } else if (version == 4) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ${ListsTable.TABLE_NAME} (
                        $_ID integer PRIMARY KEY,
                        ${ListsTable.COLUMN_NAME} text NOT NULL,
                        ${ListsTable.COLUMN_HASH} text,
                        ${ListsTable.COLUMN_NEEDS_APPROVAL} integer DEFAULT 0,
                        ${ListsTable.COLUMN_LAST_UPDATED} date
                );
                """.trimIndent()
            )
        }
    }
}
