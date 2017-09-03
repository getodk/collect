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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.provider.FormsProviderAPI;

import timber.log.Timber;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends ODKSQLiteOpenHelper {
    public static final String DATABASE_NAME = "forms.db";
    public static final String FORMS_TABLE_NAME = "forms";

    public static final String DROP_IF_EXISTS = "DROP TABLE IF EXISTS ";
    public static final String TEXT_NOT_NULL = " text not null, ";
    public static final String TEXT = " text, ";

    private static final int DATABASE_VERSION = 4;

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    public FormsDatabaseHelper(String databaseName) {
        super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onCreateNamed(db, FORMS_TABLE_NAME);
    }

    private void onCreateNamed(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " (" + FormsProviderAPI.FormsColumns._ID
                + " integer primary key, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                + TEXT_NOT_NULL + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + TEXT_NOT_NULL + FormsProviderAPI.FormsColumns.DESCRIPTION
                + TEXT
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + TEXT_NOT_NULL
                + FormsProviderAPI.FormsColumns.JR_VERSION
                + TEXT
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + TEXT_NOT_NULL
                + FormsProviderAPI.FormsColumns.DATE
                + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + TEXT_NOT_NULL
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + TEXT_NOT_NULL
                + FormsProviderAPI.FormsColumns.LANGUAGE + TEXT
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + TEXT
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + TEXT
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null);");
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
            case 3:
                success &= upgradeToVersion4(db, oldVersion);
                break;
            default:
                Timber.i("Unknown version " + oldVersion);
        }

        if (success) {
            Timber.i("Upgrading database from version " + oldVersion + " to " + newVersion + " completed with success.");
        } else {
            Timber.i("Upgrading database from version " + oldVersion + " to " + newVersion + " failed.");
        }
    }

    private boolean upgradeToVersion2(SQLiteDatabase db) {
        boolean success = true;
        try {
            db.execSQL(DROP_IF_EXISTS + FORMS_TABLE_NAME);
            onCreate(db);
        } catch (SQLiteException e) {
            Timber.e(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion4(SQLiteDatabase db, int oldVersion) {
        boolean success = true;
        try {
            // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
            // integer MODEL_VERSION to text VERSION
            db.execSQL(DROP_IF_EXISTS + TEMP_FORMS_TABLE_NAME);
            onCreateNamed(db, TEMP_FORMS_TABLE_NAME);
            db.execSQL("INSERT INTO "
                    + TEMP_FORMS_TABLE_NAME
                    + " ("
                    + FormsProviderAPI.FormsColumns._ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                    + ", "
                    + FormsProviderAPI.FormsColumns.DESCRIPTION
                    + ", "
                    + FormsProviderAPI.FormsColumns.JR_FORM_ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.MD5_HASH
                    + ", "
                    + FormsProviderAPI.FormsColumns.DATE
                    + ", " // milliseconds
                    + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH
                    + ", "
                    + FormsProviderAPI.FormsColumns.FORM_FILE_PATH
                    + ", "
                    + FormsProviderAPI.FormsColumns.LANGUAGE
                    + ", "
                    + FormsProviderAPI.FormsColumns.SUBMISSION_URI
                    + ", "
                    + FormsProviderAPI.FormsColumns.JR_VERSION
                    + ", "
                    + ((oldVersion != 3) ? ""
                    : (FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                    + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH
                    + ") SELECT "
                    + FormsProviderAPI.FormsColumns._ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                    + ", "
                    + FormsProviderAPI.FormsColumns.DESCRIPTION
                    + ", "
                    + FormsProviderAPI.FormsColumns.JR_FORM_ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.MD5_HASH
                    + ", "
                    + FormsProviderAPI.FormsColumns.DATE
                    + ", " // milliseconds
                    + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH
                    + ", "
                    + FormsProviderAPI.FormsColumns.FORM_FILE_PATH
                    + ", "
                    + FormsProviderAPI.FormsColumns.LANGUAGE
                    + ", "
                    + FormsProviderAPI.FormsColumns.SUBMISSION_URI
                    + ", "
                    + "CASE WHEN "
                    + MODEL_VERSION
                    + " IS NOT NULL THEN "
                    + "CAST("
                    + MODEL_VERSION
                    + " AS TEXT) ELSE NULL END, "
                    + ((oldVersion != 3) ? ""
                    : (FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                    + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " FROM "
                    + FORMS_TABLE_NAME);

            // risky failures here...
            db.execSQL(DROP_IF_EXISTS + FORMS_TABLE_NAME);
            onCreateNamed(db, FORMS_TABLE_NAME);
            db.execSQL("INSERT INTO "
                    + FORMS_TABLE_NAME
                    + " ("
                    + FormsProviderAPI.FormsColumns._ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                    + ", "
                    + FormsProviderAPI.FormsColumns.DESCRIPTION
                    + ", "
                    + FormsProviderAPI.FormsColumns.JR_FORM_ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.MD5_HASH
                    + ", "
                    + FormsProviderAPI.FormsColumns.DATE
                    + ", " // milliseconds
                    + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + ", "
                    + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + ", "
                    + FormsProviderAPI.FormsColumns.LANGUAGE + ", "
                    + FormsProviderAPI.FormsColumns.SUBMISSION_URI + ", "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + ", "
                    + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                    + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + ") SELECT "
                    + FormsProviderAPI.FormsColumns._ID + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                    + ", "
                    + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                    + ", "
                    + FormsProviderAPI.FormsColumns.DESCRIPTION
                    + ", "
                    + FormsProviderAPI.FormsColumns.JR_FORM_ID
                    + ", "
                    + FormsProviderAPI.FormsColumns.MD5_HASH
                    + ", "
                    + FormsProviderAPI.FormsColumns.DATE
                    + ", " // milliseconds
                    + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + ", "
                    + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + ", "
                    + FormsProviderAPI.FormsColumns.LANGUAGE + ", "
                    + FormsProviderAPI.FormsColumns.SUBMISSION_URI + ", "
                    + FormsProviderAPI.FormsColumns.JR_VERSION + ", "
                    + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                    + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " FROM "
                    + TEMP_FORMS_TABLE_NAME);
            db.execSQL(DROP_IF_EXISTS + TEMP_FORMS_TABLE_NAME);
        } catch (SQLiteException e) {
            Timber.e(e);
            success = false;
        }

        return success;
    }
}
