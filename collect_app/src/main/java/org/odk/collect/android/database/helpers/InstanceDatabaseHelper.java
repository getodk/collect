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

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.QueryBuilder;
import org.odk.collect.android.provider.InstanceProviderAPI;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.InstanceProvider.INSTANCES_TABLE_NAME;
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
public class InstanceDatabaseHelper extends ODKSQLiteOpenHelper {
    public static final String DATABASE_NAME = "instances.db";

    private String[] instancesTableColumnsInVersion4 = new String[] {_ID, DISPLAY_NAME, SUBMISSION_URI, CAN_EDIT_WHEN_COMPLETE,
            INSTANCE_FILE_PATH, JR_FORM_ID, JR_VERSION, STATUS, LAST_STATUS_CHANGE_DATE, DISPLAY_SUBTEXT, DELETED_DATE};

    private static final int DATABASE_VERSION = 4;

    public InstanceDatabaseHelper() {
        super(Collect.METADATA_PATH, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createInstancesTableForVersion4(db, INSTANCES_TABLE_NAME);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading database from version %d to %d" + ", which will destroy all old data", oldVersion, newVersion);

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
                Timber.i("Unknown version " + newVersion);
        }

        if (success) {
            Timber.i("Upgrading database from version " + oldVersion + " to " + newVersion + " completed with success.");
        } else {
            Timber.i("Upgrading database from version " + oldVersion + " to " + newVersion + " failed.");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean success = true;
        switch (newVersion) {
            case 4:
                success = downgradeToVersion4(db);
                break;

            default:
                Timber.i("Unknown version " + newVersion);
        }

        if (success) {
            Timber.i("Downgrading database completed with success.");
        } else {
            Timber.i("Downgrading database from version " + oldVersion + " to " + newVersion + " failed.");
        }
    }

    private boolean upgradeToVersion2(SQLiteDatabase db) {
        boolean success = true;
        try {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text;");
            db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET "
                    + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true)
                    + "' WHERE " + InstanceProviderAPI.InstanceColumns.STATUS + " IS NOT NULL AND "
                    + InstanceProviderAPI.InstanceColumns.STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE
                    + "'");
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion3(SQLiteDatabase db) {
        boolean success = true;
        try {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + " text;");
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion4(SQLiteDatabase db) {
        boolean success = true;
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + INSTANCES_TABLE_NAME + " LIMIT 0", null);
            int columnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE);
            cursor.close();

            // Only add the column if it doesn't already exist
            if (columnIndex == -1) {
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                        + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " date;");
            }
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private boolean downgradeToVersion4(SQLiteDatabase db) {
        boolean success = true;
        String temporaryTable = INSTANCES_TABLE_NAME + "_tmp";

        try {
            QueryBuilder
                    .begin(db)
                    .renameTable(INSTANCES_TABLE_NAME)
                    .to(temporaryTable)
                    .end();

            createInstancesTableForVersion4(db, INSTANCES_TABLE_NAME);

            QueryBuilder
                    .begin(db)
                    .insertInto(INSTANCES_TABLE_NAME)
                    .columnsForInsert(instancesTableColumnsInVersion4)
                    .select()
                    .columnsForSelect(instancesTableColumnsInVersion4)
                    .from(temporaryTable)
                    .end();

            QueryBuilder
                    .begin(db)
                    .dropIfExists(temporaryTable)
                    .end();
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private void createInstancesTableForVersion4(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + InstanceProviderAPI.InstanceColumns.SUBMISSION_URI + " text, "
                + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
                + InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_VERSION + " text, "
                + InstanceProviderAPI.InstanceColumns.STATUS + " text not null, "
                + LAST_STATUS_CHANGE_DATE + " date not null, "
                + InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT + " text not null,"
                + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " date );");
    }
}