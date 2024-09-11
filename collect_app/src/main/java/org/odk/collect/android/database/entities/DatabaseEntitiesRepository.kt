package org.odk.collect.android.database.entities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import org.odk.collect.db.sqlite.CursorExt.first
import org.odk.collect.db.sqlite.CursorExt.foldAndClose
import org.odk.collect.db.sqlite.CursorExt.getInt
import org.odk.collect.db.sqlite.CursorExt.getString
import org.odk.collect.db.sqlite.CursorExt.getStringOrNull
import org.odk.collect.db.sqlite.CursorExt.rowToMap
import org.odk.collect.db.sqlite.DatabaseMigrator
import org.odk.collect.db.sqlite.SQLiteColumns.ROW_ID
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.delete
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.doesColumnExist
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.getColumnNames
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.query
import org.odk.collect.db.sqlite.SynchronizedDatabaseConnection
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

private object ListsTable {
    const val TABLE_NAME = "lists"
    const val COLUMN_NAME = "name"
    const val COLUMN_HASH = "hash"
}

private object EntitiesTable {
    const val COLUMN_ID = "id"
    const val COLUMN_LABEL = "label"
    const val COLUMN_VERSION = "version"
    const val COLUMN_TRUNK_VERSION = "trunk_version"
    const val COLUMN_BRANCH_ID = "branch_id"
    const val COLUMN_STATE = "state"
}

class DatabaseEntitiesRepository(context: Context, dbPath: String) : EntitiesRepository {

    private val databaseConnection = SynchronizedDatabaseConnection(
        context,
        dbPath,
        "entities.db",
        EntitiesDatabaseMigrator(),
        1
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
                        list,
                        "${EntitiesTable.COLUMN_ID} = ?",
                        arrayOf(entity.id)
                    ).first { mapCursorRowToEntity(list, it, 0) }
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
                        list,
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
                        list,
                        null,
                        contentValues
                    )
                }
            }
        }

        updateRowIdTables()
    }

    override fun getLists(): Set<String> {
        return databaseConnection.withConnection {
            readableDatabase
                .query(ListsTable.TABLE_NAME)
                .foldAndClose(emptySet()) { set, cursor -> set + cursor.getString(ListsTable.COLUMN_NAME) }
        }
    }

    override fun updateListHash(list: String, hash: String) {
        val contentValues = ContentValues().also {
            it.put(ListsTable.COLUMN_HASH, hash)
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

    override fun getListHash(list: String): String? {
        return databaseConnection.withConnection {
            readableDatabase
                .query(ListsTable.TABLE_NAME, "${ListsTable.COLUMN_NAME} = ?", arrayOf(list))
                .first { it.getStringOrNull(ListsTable.COLUMN_HASH) }
        }
    }

    override fun getEntities(list: String): List<Entity.Saved> {
        if (!listExists(list)) {
            return emptyList()
        }

        return queryWithAttachedRowId(list).foldAndClose {
            mapCursorRowToEntity(
                list,
                it,
                it.getInt(ROW_ID)
            )
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
                FROM $list
                """.trimIndent(),
                null
            ).first {
                it.getInt(0)
            }!!
        }
    }

    override fun clear() {
        databaseConnection.withConnection {
            getLists().forEach {
                writableDatabase.delete(it)
            }

            writableDatabase.delete(ListsTable.TABLE_NAME)
        }
    }

    override fun addList(list: String) {
        if (!listExists(list)) {
            createList(list)
            updateRowIdTables()
        }
    }

    override fun delete(id: String) {
        databaseConnection.withConnection {
            getLists().forEach {
                writableDatabase.delete(
                    it,
                    "${EntitiesTable.COLUMN_ID} = ?",
                    arrayOf(id)
                )
            }
        }

        updateRowIdTables()
    }

    override fun getById(list: String, id: String): Entity.Saved? {
        if (!listExists(list)) {
            return null
        }

        return queryWithAttachedRowId(
            list,
            selectionColumn = EntitiesTable.COLUMN_ID,
            selectionArg = id
        ).first {
            mapCursorRowToEntity(list, it, it.getInt(ROW_ID))
        }
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        if (!listExists(list)) {
            return emptyList()
        }

        val propertyExists = databaseConnection.withConnection {
            readableDatabase.doesColumnExist(list, property)
        }

        return if (propertyExists) {
            queryWithAttachedRowId(
                list,
                selectionColumn = property,
                selectionArg = value
            ).foldAndClose {
                mapCursorRowToEntity(list, it, it.getInt(ROW_ID))
            }
        } else if (value == "") {
            queryWithAttachedRowId(list).foldAndClose {
                mapCursorRowToEntity(list, it, it.getInt(ROW_ID))
            }
        } else {
            emptyList()
        }
    }

    override fun getByIndex(list: String, index: Int): Entity.Saved? {
        if (!listExists(list)) {
            return null
        }

        return databaseConnection.withConnection {
            readableDatabase
                .rawQuery(
                    """
                    SELECT *, i.$ROW_ID
                    FROM $list e, ${getRowIdTableName(list)} i
                    WHERE e._id = i._id AND i.$ROW_ID = ?
                    """.trimIndent(),
                    arrayOf((index + 1).toString())
                ).first {
                    mapCursorRowToEntity(list, it, it.getInt(ROW_ID))
                }
        }
    }

    private fun queryWithAttachedRowId(list: String): Cursor {
        return databaseConnection.withConnection {
            readableDatabase
                .rawQuery(
                    """
                    SELECT *, i.$ROW_ID
                    FROM $list e, ${getRowIdTableName(list)} i
                    WHERE e._id = i._id
                    ORDER BY i.$ROW_ID
                    """.trimIndent(),
                    null
                )
        }
    }

    private fun queryWithAttachedRowId(
        list: String,
        selectionColumn: String,
        selectionArg: String
    ): Cursor {
        return databaseConnection.withConnection {
            readableDatabase.rawQuery(
                """
                SELECT *, i.$ROW_ID
                FROM $list e, ${getRowIdTableName(list)} i
                WHERE e._id = i._id AND $selectionColumn = ?
                ORDER BY i.$ROW_ID
                """.trimIndent(),
                arrayOf(selectionArg)
            )
        }
    }

    /**
     * Dropping and recreating this table on every change allows to maintain a sequential
     * "positions" for each entity that can be used as [Entity.Saved.index]. This method appears
     * to be faster than using a nested query to generate these at query time (calculating how many
     * _ids are higher than each entity _id). This might be replaceable with SQLite's `row_number()`
     * function, but that's not available in all the supported versions of Android.
     */
    private fun updateRowIdTables() {
        databaseConnection.withConnection {
            getLists().forEach {
                writableDatabase.execSQL(
                    """
                    DROP TABLE IF EXISTS ${getRowIdTableName(it)};
                    """.trimIndent()
                )

                writableDatabase.execSQL(
                    """
                    CREATE TABLE ${getRowIdTableName(it)} AS SELECT _id FROM $it ORDER BY _id;
                    """.trimIndent()
                )
            }
        }
    }

    private fun getRowIdTableName(it: String) = "${it}_row_numbers"

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
        databaseConnection.transaction {
            val contentValues = ContentValues()
            contentValues.put(ListsTable.COLUMN_NAME, list)
            insertOrThrow(
                ListsTable.TABLE_NAME,
                null,
                contentValues
            )

            execSQL(
                """
                CREATE TABLE IF NOT EXISTS $list (
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
                CREATE UNIQUE INDEX IF NOT EXISTS ${list}_unique_id_index ON $list (${EntitiesTable.COLUMN_ID});
                """.trimIndent()
            )
        }
    }

    private fun updatePropertyColumns(list: String, entity: Entity) {
        val columnNames = databaseConnection.withConnection {
            readableDatabase.getColumnNames(list)
        }

        val missingColumns =
            entity.properties.map { it.first }.filterNot { columnNames.contains(it) }
        if (missingColumns.isNotEmpty()) {
            databaseConnection.resetTransaction {
                missingColumns.forEach {
                    execSQL(
                        """
                        ALTER TABLE $list ADD "$it" text NOT NULL DEFAULT "";
                        """.trimIndent()
                    )
                }
            }
        }
    }

    private fun mapCursorRowToEntity(
        list: String,
        cursor: Cursor,
        rowId: Int
    ): Entity.Saved {
        val map = cursor.rowToMap()

        val propertyColumns = map.keys.filter {
            !listOf(
                _ID,
                EntitiesTable.COLUMN_ID,
                EntitiesTable.COLUMN_LABEL,
                EntitiesTable.COLUMN_VERSION,
                EntitiesTable.COLUMN_TRUNK_VERSION,
                EntitiesTable.COLUMN_BRANCH_ID,
                EntitiesTable.COLUMN_STATE,
                ROW_ID
            ).contains(it)
        }

        val properties =
            propertyColumns.fold(emptyList<Pair<String, String>>()) { accum, property ->
                accum + Pair(property, map[property] ?: "")
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

    private fun addPropertiesToContentValues(contentValues: ContentValues, entity: Entity) {
        entity.properties.forEach { (name, value) ->
            contentValues.put("\"$name\"", value)
        }
    }
}

private class EntitiesDatabaseMigrator :
    DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ${ListsTable.TABLE_NAME} (
                    $_ID integer PRIMARY KEY, 
                    ${ListsTable.COLUMN_NAME} text NOT NULL,
                    ${ListsTable.COLUMN_HASH} text
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int) = Unit
}
