package org.odk.collect.android.database.entities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.provider.BaseColumns._ID
import androidx.core.database.sqlite.transaction
import org.odk.collect.androidshared.sqlite.CursorExt.first
import org.odk.collect.androidshared.sqlite.CursorExt.foldAndClose
import org.odk.collect.androidshared.sqlite.CursorExt.getInt
import org.odk.collect.androidshared.sqlite.CursorExt.getIntOrNull
import org.odk.collect.androidshared.sqlite.CursorExt.getString
import org.odk.collect.androidshared.sqlite.CursorExt.getStringOrNull
import org.odk.collect.androidshared.sqlite.DatabaseConnection
import org.odk.collect.androidshared.sqlite.DatabaseMigrator
import org.odk.collect.androidshared.sqlite.SQLiteColumns.ROW_ID
import org.odk.collect.androidshared.sqlite.SQLiteDatabaseExt.delete
import org.odk.collect.androidshared.sqlite.SQLiteDatabaseExt.query
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

private object ListsTable {
    const val TABLE_NAME = "lists"
    const val COLUMN_NAME = "name"
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

    private val databaseConnection: DatabaseConnection = DatabaseConnection(
        context,
        dbPath,
        "entities.db",
        EntitiesDatabaseMigrator(),
        1,
        true
    )

    override fun save(vararg entities: Entity) {
        val existingLists = getLists()
        val createdLists = mutableListOf<String>()
        val modifiedList = mutableListOf<String>()

        databaseConnection.writeableDatabase.transaction {
            entities.forEach { entity ->
                val list = entity.list
                if (!existingLists.contains(list) && !createdLists.contains(list)) {
                    createList(list)
                    createdLists.add(list)
                }

                if (!modifiedList.contains(list)) {
                    updatePropertyColumns(entity)
                    modifiedList.add(list)
                }

                val existing = if (existingLists.contains(list)) {
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

                        entity.properties.forEach { (name, value) ->
                            it.put(name, value)
                        }
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

                        entity.properties.forEach { (name, value) ->
                            it.put(name, value)
                        }
                    }

                    insertOrThrow(
                        list,
                        null,
                        contentValues
                    )
                }
            }
        }

        updateRowIdTable()
    }

    override fun getLists(): Set<String> {
        return databaseConnection
            .readableDatabase
            .query(ListsTable.TABLE_NAME)
            .foldAndClose(emptySet()) { set, cursor -> set + cursor.getString(ListsTable.COLUMN_NAME) }
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

        return databaseConnection.readableDatabase.rawQuery(
            """
            SELECT COUNT(*)
            FROM $list
            """.trimIndent(),
            null
        ).first {
            it.getInt(0)
        }!!
    }

    override fun clear() {
        getLists().forEach {
            databaseConnection.writeableDatabase.delete(it)
        }

        databaseConnection.writeableDatabase.delete(ListsTable.TABLE_NAME)
    }

    override fun addList(list: String) {
        if (!listExists(list)) {
            createList(list)
            updateRowIdTable()
        }
    }

    override fun delete(id: String) {
        getLists().forEach {
            databaseConnection.writeableDatabase.delete(
                it,
                "${EntitiesTable.COLUMN_ID} = ?",
                arrayOf(id)
            )
        }

        updateRowIdTable()
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

        return queryWithAttachedRowId(
            list,
            selectionColumn = property,
            selectionArg = value
        ).foldAndClose {
            mapCursorRowToEntity(list, it, it.getInt(ROW_ID))
        }
    }

    override fun getByIndex(list: String, index: Int): Entity.Saved? {
        if (!listExists(list)) {
            return null
        }

        return databaseConnection.readableDatabase
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

    private fun queryWithAttachedRowId(list: String): Cursor {
        return databaseConnection.readableDatabase
            .rawQuery(
                """
                SELECT *, i.$ROW_ID
                FROM $list e, ${getRowIdTableName(list)} i
                WHERE e._id = i._id
                """.trimIndent(),
                null
            )
    }

    private fun queryWithAttachedRowId(
        list: String,
        selectionColumn: String,
        selectionArg: String
    ): Cursor {
        return databaseConnection.readableDatabase
            .rawQuery(
                """
                SELECT *, i.$ROW_ID
                FROM $list e, ${getRowIdTableName(list)} i
                WHERE e._id = i._id AND $selectionColumn = ?
                """.trimIndent(),
                arrayOf(selectionArg)
            )
    }

    /**
     * Dropping and recreating this table on every change allows to maintain a sequential
     * "positions" for each entity that can be used as [Entity.Saved.index]. This method appears
     * to be faster than using a nested query to generate these at query time (calculating how many
     * _ids are higher than each entity _id). This might be replaceable with SQLite's `row_number()`
     * function, but that's not available in all the supported versions of Android.
     */
    private fun updateRowIdTable() {
        getLists().forEach {
            databaseConnection.writeableDatabase.execSQL(
                """
                DROP TABLE IF EXISTS ${getRowIdTableName(it)};
                """.trimIndent()
            )

            databaseConnection.writeableDatabase.execSQL(
                """
                CREATE TABLE ${getRowIdTableName(it)} AS SELECT _id FROM $it;
                """.trimIndent()
            )
        }
    }

    private fun getRowIdTableName(it: String) = "${it}_row_numbers"

    private fun listExists(list: String): Boolean {
        return databaseConnection.readableDatabase
            .query(
                ListsTable.TABLE_NAME,
                selection = "${ListsTable.COLUMN_NAME} = ?",
                selectionArgs = arrayOf(list)
            ).use { it.count } > 0
    }

    private fun createList(list: String) {
        databaseConnection.writeableDatabase.transaction {
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

    private fun updatePropertyColumns(entity: Entity) {
        entity.properties.map { it.first }.forEach {
            try {
                databaseConnection.writeableDatabase.execSQL(
                    """
                    ALTER TABLE ${entity.list} ADD $it text;
                    """.trimIndent()
                )
            } catch (e: SQLiteException) {
                // Ignored
            }
        }
    }

    private fun mapCursorRowToEntity(
        list: String,
        cursor: Cursor,
        rowId: Int
    ): Entity.Saved {
        val propertyColumns = cursor.columnNames.filter {
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
                accum + Pair(property, cursor.getStringOrNull(property) ?: "")
            }

        val state = if (cursor.getInt(EntitiesTable.COLUMN_STATE) == 0) {
            Entity.State.OFFLINE
        } else {
            Entity.State.ONLINE
        }

        return Entity.Saved(
            list,
            cursor.getString(EntitiesTable.COLUMN_ID),
            cursor.getStringOrNull(EntitiesTable.COLUMN_LABEL),
            cursor.getInt(EntitiesTable.COLUMN_VERSION),
            properties,
            state,
            rowId - 1,
            cursor.getIntOrNull(EntitiesTable.COLUMN_TRUNK_VERSION),
            cursor.getString(EntitiesTable.COLUMN_BRANCH_ID)
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
}

private class EntitiesDatabaseMigrator :
    DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ${ListsTable.TABLE_NAME} (
                    $_ID integer PRIMARY KEY, 
                    ${ListsTable.COLUMN_NAME} text NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int) = Unit

    override fun onDowngrade(db: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }
}
