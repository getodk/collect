package org.odk.collect.android.database.instances;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseConstants.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.CAN_DELETE_BEFORE_SEND;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.STATUS;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.SUBMISSION_URI;
import static org.odk.collect.db.sqlite.SQLiteDatabaseExt.doesColumnExist;

import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.db.sqlite.DatabaseMigrator;
import org.odk.collect.db.sqlite.SQLiteDatabaseExt;
import org.odk.collect.db.sqlite.SQLiteUtils;
import org.odk.collect.forms.instances.Instance;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class InstanceDatabaseMigrator implements DatabaseMigrator {
    private static final String[] COLUMN_NAMES_V5 = {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DELETED_DATE};

    private static final String[] COLUMN_NAMES_V6 = {_ID, DISPLAY_NAME, SUBMISSION_URI,
            CAN_EDIT_WHEN_COMPLETE, INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS,
            LAST_STATUS_CHANGE_DATE, DELETED_DATE, GEOMETRY, GEOMETRY_TYPE};

    public void onCreate(SQLiteDatabase db) {
        createInstancesTableV8(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) {
        Timber.w("Instances db upgrade from version: %s", oldVersion);
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
            case 6:
                upgradeToVersion7(db);
            case 7:
                upgradeToVersion8(db);
            case 8:
                // Remember to bump the database version number in {@link org.odk.collect.android.database.DatabaseConstants}
                // upgradeToVersion9(db);
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        if (!doesColumnExist(db, INSTANCES_TABLE_NAME, CAN_EDIT_WHEN_COMPLETE)) {
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
     * column which was redundant with the {@link DatabaseInstanceColumns#STATUS} and
     * {@link DatabaseInstanceColumns#LAST_STATUS_CHANGE_DATE} columns and included
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
        List<String> columns = SQLiteDatabaseExt.getColumnNames(db, INSTANCES_TABLE_NAME);
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

    private void upgradeToVersion7(SQLiteDatabase db) {
        String temporaryTable = INSTANCES_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, INSTANCES_TABLE_NAME, temporaryTable);
        createInstancesTableV7(db);
        SQLiteUtils.copyRows(db, temporaryTable, COLUMN_NAMES_V6, INSTANCES_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, INSTANCES_TABLE_NAME, CAN_DELETE_BEFORE_SEND, "text");
        db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET " + CAN_DELETE_BEFORE_SEND + " = 'true';");
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

    public void createInstancesTableV6(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + INSTANCES_TABLE_NAME + " ("
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
                + GEOMETRY + " text, "
                + GEOMETRY_TYPE + " text);");
    }

    public void createInstancesTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + INSTANCES_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + DISPLAY_NAME + " text not null, "
                + SUBMISSION_URI + " text, "
                + CAN_EDIT_WHEN_COMPLETE + " text, "
                + INSTANCE_FILE_PATH + " text not null, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + DELETED_DATE + " date, "
                + GEOMETRY + " text, "
                + GEOMETRY_TYPE + " text);");
    }

    public void createInstancesTableV8(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + INSTANCES_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + DISPLAY_NAME + " text not null, "
                + SUBMISSION_URI + " text, "
                + CAN_EDIT_WHEN_COMPLETE + " text, "
                + CAN_DELETE_BEFORE_SEND + " text, "
                + INSTANCE_FILE_PATH + " text not null, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + DELETED_DATE + " date, "
                + GEOMETRY + " text, "
                + GEOMETRY_TYPE + " text);");
    }
}
