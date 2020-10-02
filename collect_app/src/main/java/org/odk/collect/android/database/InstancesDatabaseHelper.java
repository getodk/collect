/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of theø License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.CustomSQLiteQueryExecutor;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.ACT_LAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.ACT_LON;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.FORM_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.GEOMETRY;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.GEOMETRY_TYPE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SCHED_LAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SCHED_LON;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.SOURCE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ACT_FINISH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ACT_START;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ADDRESS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_ASS_ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_HIDE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_IS_SYNC;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_LOCATION_TRIGGER;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_REPEAT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SCHED_FINISH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SCHED_START;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SHOW_DIST;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_SURVEY_NOTES;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TASK_COMMENT;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TASK_STATUS;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_TITLE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_UPDATED;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.T_UPDATEID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.UUID;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class InstancesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "instances.db";
    public static final String INSTANCES_TABLE_NAME = "instances";

    public static final int DATABASE_VERSION = 16;     // smap

    private static final String[] COLUMN_NAMES_V5 = {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DELETED_DATE};

    private static final String[] COLUMN_NAMES_V6 = {_ID, DISPLAY_NAME, SUBMISSION_URI,
        CAN_EDIT_WHEN_COMPLETE, INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS,
        LAST_STATUS_CHANGE_DATE, DELETED_DATE, GEOMETRY, GEOMETRY_TYPE};

    // smap
    private static final String[] COLUMN_NAMES_V16 = new String[] {
            _ID,
            DISPLAY_NAME,
            SUBMISSION_URI,
            CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH,
            JR_FORM_ID,
            JR_VERSION,
            STATUS,
            LAST_STATUS_CHANGE_DATE,
            DELETED_DATE,
            SOURCE,             // smap
            FORM_PATH,          // smap
            ACT_LON,            // smap
            ACT_LAT,            // smap
            SCHED_LON,          // smap
            SCHED_LAT,          // smap
            T_TITLE,            // smap
            T_SCHED_START,      // smap
            T_SCHED_FINISH,      // smap
            T_ACT_START,        // smap
            T_ACT_FINISH,       // smap
            T_ADDRESS,          // smap
            GEOMETRY,             // smap
            GEOMETRY_TYPE,        // smap
            T_IS_SYNC,          // smap
            T_ASS_ID,           // smap
            T_TASK_STATUS,      // smap
            T_TASK_COMMENT,     // smap
            T_REPEAT,           // smap
            T_UPDATEID,         // smap
            T_LOCATION_TRIGGER, // smap
            T_SURVEY_NOTES,     // smap
             UUID,               // smap
            T_UPDATED,          // smap
            T_SHOW_DIST,        // smap
            T_HIDE              // smap
    };

    public static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V16;  // smap

    private static boolean isDatabaseBeingMigrated;

    public InstancesDatabaseHelper() {
        super(new DatabaseContext(new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA)), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getDatabasePath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA) + File.separator + DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createInstancesTableV16(db, INSTANCES_TABLE_NAME);  // smap
        //upgradeToVersion6(db, INSTANCES_TABLE_NAME);      // smap commented
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

            // smap
            if(oldVersion < 16) {
                upgradeToVersion16(db);
            }
            /*
            switch (oldVersion) {
                case 1:
                    upgradeToVersion2(db);
                case 2:
                    upgradeToVersion3(db);
                case 3:
                    upgradeToVersion4(db);
                case 4:
                    upgradeToVersion5(db);
                case 5:
                    upgradeToVersion6(db, INSTANCES_TABLE_NAME);
                    break;
                default:
                    // Timber.i("Unknown version %d", oldVersion); // smap commented
            }
             */

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

            String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";
            createInstancesTableV5(db, temporaryTableName);
            upgradeToVersion6(db, temporaryTableName);

            dropObsoleteColumns(db, CURRENT_VERSION_COLUMN_NAMES, temporaryTableName);
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
                    + STATUS + " != '" + Instance.STATUS_INCOMPLETE
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
     * Upgrade to version 5. Prior versions of the instances table included a {@code displaySubtext}
     * column which was redundant with the {@link org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns#STATUS} and
     * {@link org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns#LAST_STATUS_CHANGE_DATE} columns and included
     * unlocalized text. Version 5 removes this column.
     */
    private void upgradeToVersion5(SQLiteDatabase db) {
        String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";

        // onDowngrade in Collect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        SQLiteUtils.dropTable(db, temporaryTableName);

        createInstancesTableV5(db, temporaryTableName);
        dropObsoleteColumns(db, COLUMN_NAMES_V5, temporaryTableName);
    }

    private void upgradeToVersion16(SQLiteDatabase db) {
        String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";

        SQLiteUtils.dropTable(db, temporaryTableName);
        createInstancesTableV16(db, temporaryTableName);
        dropObsoleteColumns(db, COLUMN_NAMES_V16, temporaryTableName);

    }

    /**
     * Use the existing temporary table with the provided name to only keep the given relevant
     * columns, dropping all others.
     *
     * NOTE: the temporary table with the name provided is dropped.
     *
     * The move and copy strategy is used to overcome the fact that SQLITE does not directly support
     * removing a column. See https://sqlite.org/lang_altertable.html
     *
     * @param db                    the database to operate on
     * @param relevantColumns       the columns relevant to the current version
     * @param temporaryTableName    the name of the temporary table to use and then drop
     */
    private void dropObsoleteColumns(SQLiteDatabase db, String[] relevantColumns, String temporaryTableName) {
        List<String> columns = SQLiteUtils.getColumnNames(db, INSTANCES_TABLE_NAME);
        columns.retainAll(Arrays.asList(relevantColumns));
        String[] columnsToKeep = columns.toArray(new String[0]);

        SQLiteUtils.copyRows(db, INSTANCES_TABLE_NAME, columnsToKeep, temporaryTableName);
        SQLiteUtils.dropTable(db, INSTANCES_TABLE_NAME);
        SQLiteUtils.renameTable(db, temporaryTableName, INSTANCES_TABLE_NAME);
    }

    private void upgradeToVersion6(SQLiteDatabase db, String name) {
        SQLiteUtils.addColumn(db, name, GEOMETRY, "text");
        SQLiteUtils.addColumn(db, name, GEOMETRY_TYPE, "text");
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

    // smap
    private static void createInstancesTableV16(SQLiteDatabase db, String name) {
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
                + DELETED_DATE + " date, "
                + SOURCE + " text, "		    // smap
                + FORM_PATH + " text, "		    // smap
                + ACT_LON + " double, "		    // smap
                + ACT_LAT + " double, "		    // smap
                + SCHED_LON + " double, "		// smap
                + SCHED_LAT + " double, "		// smap
                + T_TITLE + " text, "		    // smap
                + T_SCHED_START + " long, "		// smap
                + T_SCHED_FINISH + " long, "	// smap
                + T_ACT_START + " long, "		// smap
                + T_ACT_FINISH + " long, "		// smap
                + T_ADDRESS + " text, "		    // smap
                + GEOMETRY + " text, "		    // smap
                + GEOMETRY_TYPE + " text, "		// smap
                + T_IS_SYNC + " text, "		    // smap
                + T_ASS_ID + " long, "		    // smap
                + T_TASK_STATUS + " text, "		// smap
                + T_TASK_COMMENT + " text, "    // smap
                + T_REPEAT + " integer, "		// smap
                + T_UPDATEID + " text, "		// smap
                + T_LOCATION_TRIGGER + " text, " // smap
                + T_SURVEY_NOTES + " text, "    // smap
                + UUID + " text, "		        // smap
                + T_UPDATED + " integer, "      // smap
                + T_SHOW_DIST + " integer, "    // smap
                + T_HIDE + " integer, "         // smap

                + "displaySubtext text "   // Smap keep for downgrading
                + ");");
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
            SQLiteDatabase db = SQLiteDatabase.openDatabase(InstancesDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY);
            isDatabaseHelperOutOfDate = InstancesDatabaseHelper.DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }

    // smap
    public static void recreateDatabase() {

        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(InstancesDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READWRITE);
            SQLiteUtils.dropTable(db, INSTANCES_TABLE_NAME);
            createInstancesTableV16(db, INSTANCES_TABLE_NAME);
            db.close();

        } catch (SQLException e) {
            Timber.i(e);
        }
    }
}
