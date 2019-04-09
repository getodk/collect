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
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DELETED_DATE;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT;
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
    public static final String INSTANCES_TABLE_NAME = "instances";

    private static final int DATABASE_VERSION = 4;

    private final String[] instancesTableColumnsInVersion4 = new String[] {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DISPLAY_SUBTEXT, DELETED_DATE};

    public InstancesDatabaseHelper() {
        super(new DatabaseContext(Collect.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createInstancesTable(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

        boolean success = true;
        switch (oldVersion) {
            case 1:
                success = upgradeToVersion2(db);
            case 2:
                success &= upgradeToVersion3(db);
            case 3:
                success &= upgradeToVersion4(db);
                break;
            default:
                Timber.i("Unknown version " + oldVersion);
        }

        if (success) {
            Timber.i("Upgrading database from version " + oldVersion + " to " + newVersion + " completed with success.");
        } else {
            Timber.e("Upgrading database from version " + oldVersion + " to " + newVersion + " failed.");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean success = true;
        switch (newVersion) {
            case 4:
                success = downgrade(db, instancesTableColumnsInVersion4);
                break;

            default:
                Timber.i("Unknown version " + newVersion);
        }

        if (success) {
            Timber.i("Downgrading database completed with success.");
        } else {
            Timber.e("Downgrading database from version " + oldVersion + " to " + newVersion + " failed.");
        }
    }

    private boolean upgradeToVersion2(SQLiteDatabase db) {
        boolean success = true;
        try {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + CAN_EDIT_WHEN_COMPLETE + " text;");
            db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET "
                    + CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true)
                    + "' WHERE " + STATUS + " IS NOT NULL AND "
                    + STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE
                    + "'");
        } catch (SQLiteException e) {
            Timber.e(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion3(SQLiteDatabase db) {
        boolean success = true;
        try {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + JR_VERSION + " text;");
        } catch (SQLiteException e) {
            Timber.e(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion4(SQLiteDatabase db) {
        boolean success = true;
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + INSTANCES_TABLE_NAME + " LIMIT 0", null);
            int columnIndex = cursor.getColumnIndex(DELETED_DATE);
            cursor.close();

            // Only add the column if it doesn't already exist
            if (columnIndex == -1) {
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                        + DELETED_DATE + " date;");
            }
        } catch (SQLiteException e) {
            Timber.e(e);
            success = false;
        }
        return success;
    }

    private boolean downgrade(SQLiteDatabase db, String[] instancesTableColumns) {
        boolean success = true;
        String temporaryTable = INSTANCES_TABLE_NAME + "_tmp";

        try {
            CustomSQLiteQueryBuilder
                    .begin(db)
                    .renameTable(INSTANCES_TABLE_NAME)
                    .to(temporaryTable)
                    .end();

            createInstancesTable(db);

            // Try to avoid renaming columns in the future since restoring data after downgrade might not work
            CustomSQLiteQueryBuilder
                    .begin(db)
                    .insertInto(INSTANCES_TABLE_NAME)
                    .columnsForInsert(instancesTableColumns)
                    .select()
                    .columnsForSelect(instancesTableColumns)
                    .from(temporaryTable)
                    .end();

            CustomSQLiteQueryBuilder
                    .begin(db)
                    .dropIfExists(temporaryTable)
                    .end();
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private void createInstancesTable(SQLiteDatabase db) {
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
                + DISPLAY_SUBTEXT + " text not null,"
                + DELETED_DATE + " date );");
    }
}