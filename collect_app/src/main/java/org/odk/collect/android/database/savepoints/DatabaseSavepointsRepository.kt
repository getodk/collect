package org.odk.collect.android.database.savepoints

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.os.StrictMode
import org.odk.collect.android.database.DatabaseConnection
import org.odk.collect.android.database.DatabaseConstants
import org.odk.collect.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_NAME
import org.odk.collect.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_VERSION
import org.odk.collect.android.database.savepoints.DatabaseSavepointObjectMapper.getSavepointFromCurrentCursorPosition
import org.odk.collect.android.database.savepoints.DatabaseSavepointObjectMapper.getValuesFromSavepoint
import org.odk.collect.android.database.savepoints.DatabaseSavepointsColumns.FORM_DB_ID
import org.odk.collect.android.database.savepoints.DatabaseSavepointsColumns.INSTANCE_DB_ID
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import java.io.File

class DatabaseSavepointsRepository(
    context: Context,
    dbPath: String,
    private val cachePath: String,
    private val instancesPath: String
) : SavepointsRepository {
    private val databaseConnection: DatabaseConnection = DatabaseConnection(
        context,
        dbPath,
        SAVEPOINTS_DATABASE_NAME,
        SavepointsDatabaseMigrator(),
        SAVEPOINTS_DATABASE_VERSION
    )

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        StrictMode.noteSlowCall("Accessing readable DB")

        val cursor = if (instanceDbId == null) {
            queryAndReturnCursor(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID IS NULL",
                arrayOf(formDbId.toString())
            )
        } else {
            queryAndReturnCursor(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?",
                arrayOf(formDbId.toString(), instanceDbId.toString())
            )
        }
        val savepoints = getSavepointsFromCursor(cursor)

        return if (savepoints.isNotEmpty()) savepoints[0] else null
    }

    override fun getAll(): List<Savepoint> {
        val cursor = queryAndReturnCursor()
        return getSavepointsFromCursor(cursor)
    }

    override fun save(savepoint: Savepoint) {
        if (get(savepoint.formDbId, savepoint.instanceDbId) != null) {
            return
        }

        val values = getValuesFromSavepoint(savepoint, cachePath, instancesPath)

        databaseConnection
            .writeableDatabase
            .insertOrThrow(DatabaseConstants.SAVEPOINTS_TABLE_NAME, null, values)
    }

    override fun delete(formDbId: Long, instanceDbId: Long?) {
        val savepoint = get(formDbId, instanceDbId) ?: return

        val (selection, selectionArgs) = if (savepoint.instanceDbId == null) {
            Pair(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID IS NULL",
                arrayOf(savepoint.formDbId.toString())
            )
        } else {
            Pair(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?",
                arrayOf(savepoint.formDbId.toString(), savepoint.instanceDbId.toString())
            )
        }

        databaseConnection
            .writeableDatabase
            .delete(DatabaseConstants.SAVEPOINTS_TABLE_NAME, selection, selectionArgs)

        File(savepoint.savepointFilePath).delete()
    }

    override fun deleteAll() {
        getAll().forEach {
            File(it.savepointFilePath).delete()
        }

        databaseConnection
            .writeableDatabase
            .delete(DatabaseConstants.SAVEPOINTS_TABLE_NAME, null, null)
    }

    private fun queryAndReturnCursor(selection: String? = null, selectionArgs: Array<String?>? = null): Cursor {
        val readableDatabase = databaseConnection.readableDatabase
        val qb = SQLiteQueryBuilder().apply {
            tables = DatabaseConstants.SAVEPOINTS_TABLE_NAME
        }
        return qb.query(readableDatabase, null, selection, selectionArgs, null, null, null)
    }

    private fun getSavepointsFromCursor(cursor: Cursor?): List<Savepoint> {
        val savepoints: MutableList<Savepoint> = ArrayList()
        if (cursor != null) {
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                val savepoint = getSavepointFromCurrentCursorPosition(cursor, cachePath, instancesPath)
                savepoints.add(savepoint)
            }
        }
        return savepoints
    }
}
