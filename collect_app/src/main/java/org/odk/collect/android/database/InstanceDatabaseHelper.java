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

package org.odk.collect.android.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;

import timber.log.Timber;

import static org.odk.collect.android.provider.InstanceProvider.INSTANCES_TABLE_NAME;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class InstanceDatabaseHelper extends ODKSQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;


    public InstanceDatabaseHelper(String databaseName) {
        super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + INSTANCES_TABLE_NAME + " ("
                + InstanceProviderAPI.InstanceColumns._ID + " integer primary key, "
                + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " text not null, "
                + InstanceProviderAPI.InstanceColumns.SUBMISSION_URI + " text, "
                + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
                + InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_VERSION + " text, "
                + InstanceProviderAPI.InstanceColumns.STATUS + " text not null, "
                + InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " date not null, "
                + InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT + " text not null,"
                + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " date );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final int initialVersion = oldVersion;
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text;");
            db.execSQL("UPDATE " + INSTANCES_TABLE_NAME + " SET "
                    + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true)
                    + "' WHERE " + InstanceProviderAPI.InstanceColumns.STATUS + " IS NOT NULL AND "
                    + InstanceProviderAPI.InstanceColumns.STATUS + " != '" + InstanceProviderAPI.STATUS_INCOMPLETE
                    + "'");
            oldVersion = 2;
        }
        if (oldVersion == 2) {
            db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + " text;");
        }
        if (oldVersion == 3) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + INSTANCES_TABLE_NAME + " LIMIT 0", null);
            int columnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE);
            cursor.close();

            // Only add the column if it doesn't already exist
            if (columnIndex == -1) {
                db.execSQL("ALTER TABLE " + INSTANCES_TABLE_NAME + " ADD COLUMN "
                        + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " date;");
            }
        }
        Timber.w("Successfully upgraded database from version %d to %d, without destroying all the old data",
                initialVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}