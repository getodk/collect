package org.odk.collect.android.database.savepoints

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import org.odk.collect.android.database.DatabaseConstants.SAVEPOINTS_TABLE_NAME
import org.odk.collect.android.database.DatabaseMigrator
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.FORM_DB_ID
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.INSTANCE_DB_ID
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.INSTANCE_FILE_PATH
import org.odk.collect.android.database.savepoints.SavepointsDatabaseColumns.SAVEPOINT_FILE_PATH
import org.odk.collect.android.utilities.SQLiteUtils

class SavepointsDatabaseMigrator : DatabaseMigrator {
    override fun onCreate(db: SQLiteDatabase) {
        createSavepointsTableV1(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int) {
        when (oldVersion) {
            1 -> {
                // Remember to bump the database version number in {@link org.odk.collect.android.database.DatabaseConstants}
                // upgradeToVersion2(db);
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase) {
        SQLiteUtils.dropTable(db, SAVEPOINTS_TABLE_NAME)
        createSavepointsTableV1(db)
    }

    private fun createSavepointsTableV1(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $SAVEPOINTS_TABLE_NAME (" +
                "$_ID integer PRIMARY KEY, " +
                "$FORM_DB_ID integer NOT NULL, " +
                "$INSTANCE_DB_ID integer, " +
                "$SAVEPOINT_FILE_PATH text NOT NULL, " +
                "$INSTANCE_FILE_PATH text NOT NULL);"
        )
    }
}
