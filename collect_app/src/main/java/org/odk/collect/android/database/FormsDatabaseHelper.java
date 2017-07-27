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

import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI;

import timber.log.Timber;

import static org.odk.collect.android.provider.FormsProvider.FORMS_TABLE_NAME;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends ODKSQLiteOpenHelper {
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
                + " text not null, " + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + " text not null, " + FormsProviderAPI.FormsColumns.DESCRIPTION
                + " text, "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + " text not null, "
                + FormsProviderAPI.FormsColumns.JR_VERSION
                + " text, "
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + " text not null, "
                + FormsProviderAPI.FormsColumns.DATE
                + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.LANGUAGE + " text, "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + " text, "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            Timber.w("Upgrading database from version %d to %d"
                    + ", which will destroy all old data", oldVersion, newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE_NAME);
            onCreate(db);
            return;
        } else {
            // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
            // integer MODEL_VERSION to text VERSION
            db.execSQL("DROP TABLE IF EXISTS " + TEMP_FORMS_TABLE_NAME);
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
            db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE_NAME);
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
            db.execSQL("DROP TABLE IF EXISTS " + TEMP_FORMS_TABLE_NAME);

            Timber.w("Successfully upgraded database from version %d to %d"
                    + ", without destroying all the old data", oldVersion, newVersion);
        }
    }
}