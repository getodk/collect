/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.database.helpers;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class InstancesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "instances.db";
    public static final String DATABASE_PATH = Collect.METADATA_PATH + File.separator + DATABASE_NAME;
    public static final String INSTANCES_TABLE_NAME = "instances";

    static final int DATABASE_VERSION = 5;

    private static final String[] COLUMN_NAMES_V5 = {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DELETED_DATE};
    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V5;

    private static boolean isDatabaseBeingMigrated;

    public InstancesDatabaseHelper() {
        super(new DatabaseContext(Collect.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createInstancesTableV5(db, INSTANCES_TABLE_NAME);
    }

    /**
     * Upgrades the database.
     *
     * When a new migration is added, a corresponding test case should be added to
     * InstancesDatabaseHelperTest by copying a real database into assets.
     */
    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            switch (oldVersion) {
                case 1:
                    upgradeToVersion2(db);
                case 2:
                    upgradeToVersion3(db);
                case 3:
                    upgradeToVersion4(db);
                case 4:
                    moveInstancesTableToVersion5(db);
                    break;
                default:
                    Timber.i("Unknown version %d", oldVersion);
            }

            Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Downgrading database from version %d to %d", oldVersion, newVersion);
            moveInstancesTableToVersion5(db);

            Timber.i("Downgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        if (!SQLiteUtils.doesColumnExist(db, INSTANCES_TABLE_NAME, CAN_EDIT_WHEN_COMPLETE)) {
            SQLiteUtils.addColumn(db, INSTANCES_TABLE_NAME, CAN_EDIT_WHEN_COMPLETE, "text");

            db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET "
                    + CAN_EDIT_WHEN_COMPLETE + " = '" + true
                    + "' WHERE " + STATUS + " IS NOT NULL AND "
                    + STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE
                    + "'");
        }
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, INSTANCES_TABLE_NAME, JR_VERSION, "text");
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, INSTANCES_TABLE_NAME, DELETED_DATE, "date");
    }

    /**
     * Upgrade to version 5 by creating the new table with a temporary name, moving the contents of
     * the existing instances table to that new table, dropping the old table and then renaming the
     * new table to the permanent name.
     *
     * Prior versions of the instances table included a {@code displaySubtext} column which was
     * redundant with the {@link InstanceProviderAPI.InstanceColumns#STATUS} and
     * {@link InstanceProviderAPI.InstanceColumns#LAST_STATUS_CHANGE_DATE} columns and included
     * unlocalized text. Version 5 removes this column.
     *
     * The move and copy strategy is used to overcome the fact that SQLITE does not directly support
     * removing a column. See https://sqlite.org/lang_altertable.html
     */
    private void moveInstancesTableToVersion5(SQLiteDatabase db) {
        List<String> columnNamesPrev = SQLiteUtils.getColumnNames(db, INSTANCES_TABLE_NAME);

        String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";

        // onDowngrade in Collect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        SQLiteUtils.dropTable(db, temporaryTableName);

        createInstancesTableV5(db, temporaryTableName);

        // Only select columns from the existing table that are also relevant to v5
        List<String> columns = SQLiteUtils.getColumnNames(db, INSTANCES_TABLE_NAME);
        columns.retainAll(Arrays.asList(COLUMN_NAMES_V5));
        String[] columnsToKeep = columns.toArray(new String[0]);

        SQLiteUtils.copyRows(db, INSTANCES_TABLE_NAME, columnsToKeep, temporaryTableName);
        SQLiteUtils.dropTable(db, INSTANCES_TABLE_NAME);
        SQLiteUtils.renameTable(db, temporaryTableName, INSTANCES_TABLE_NAME);
    }

    private void createInstancesTableV5(SQLiteDatabase db, String name) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + SUBMISSION_URI + " text, "
                + CAN_EDIT_WHEN_COMPLETE + " text, "
                + INSTANCE_FILE_PATH + " text not null, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + DELETED_DATE + " date );");
    }

    public static void databaseMigrationStarted() {
        isDatabaseBeingMigrated = true;
    }

    public static boolean isDatabaseBeingMigrated() {
        return isDatabaseBeingMigrated;
    }

    public static boolean databaseNeedsUpgrade() {
        boolean isDatabaseHelperOutOfDate = false;
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(InstancesDatabaseHelper.DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
            isDatabaseHelperOutOfDate = InstancesDatabaseHelper.DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }
}
