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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;

import java.util.ArrayList;
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
    static final String DATABASE_NAME = "instances.db";
    public static final String INSTANCES_TABLE_NAME = "instances";

    static final int DATABASE_VERSION = 5;

    private static final String[] COLUMN_NAMES_V5 = new String[] {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DELETED_DATE};
    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V5;

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
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Downgrading database from version %d to %d", oldVersion, newVersion);
        moveInstancesTableToVersion5(db);

        Timber.i("Downgrading database from version %d to %d completed with success.", oldVersion, newVersion);
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                + CAN_EDIT_WHEN_COMPLETE + " text;");
        db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET "
                + CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true)
                + "' WHERE " + STATUS + " IS NOT NULL AND "
                + STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE
                + "'");
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + JR_VERSION + " text;");
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + INSTANCES_TABLE_NAME + " LIMIT 0", null);
        int columnIndex = cursor.getColumnIndex(DELETED_DATE);
        cursor.close();

        // Only add the column if it doesn't already exist
        if (columnIndex == -1) {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + DELETED_DATE + " date;");
        }
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
        List<String> columnNamesPrev = getInstancesColumnNames(db);

        String temporaryTableName = INSTANCES_TABLE_NAME + "_tmp";

        // onDowngrade in Collect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(temporaryTableName)
                .end();

        createInstancesTableV5(db, temporaryTableName);

        // Only select columns from the existing table that are also relevant to v5
        columnNamesPrev.retainAll(new ArrayList<>(Arrays.asList(COLUMN_NAMES_V5)));

        CustomSQLiteQueryBuilder
                .begin(db)
                .insertInto(temporaryTableName)
                .columnsForInsert(columnNamesPrev.toArray(new String[0]))
                .select()
                .columnsForSelect(columnNamesPrev.toArray(new String[0]))
                .from(INSTANCES_TABLE_NAME)
                .end();

        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(INSTANCES_TABLE_NAME)
                .end();

        CustomSQLiteQueryBuilder
                .begin(db)
                .renameTable(temporaryTableName)
                .to(INSTANCES_TABLE_NAME)
                .end();
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

    static List<String> getInstancesColumnNames(SQLiteDatabase db) {
        String[] columnNames;
        try (Cursor c = db.query(INSTANCES_TABLE_NAME, null, null, null, null, null, null)) {
            columnNames = c.getColumnNames();
        }

        // Build a full-featured ArrayList rather than the limited array-backed List from asList
        return new ArrayList<>(Arrays.asList(columnNames));
    }
}