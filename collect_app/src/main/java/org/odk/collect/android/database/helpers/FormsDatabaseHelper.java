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
import org.odk.collect.android.database.QueryBuilder;
import org.odk.collect.android.provider.FormsProviderAPI;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProvider.FORMS_TABLE_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends ODKSQLiteOpenHelper {
    private String[] formsTableColumnsInVersion4 = new String[] {_ID, DISPLAY_NAME, DISPLAY_SUBTEXT, DESCRIPTION, JR_FORM_ID, JR_VERSION,
            MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE, SUBMISSION_URI,
            BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH};

    private static final int DATABASE_VERSION = 4;

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    public FormsDatabaseHelper(String databaseName) {
        super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFormsTableForVersion4(db, FORMS_TABLE_NAME);
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
            case 3:
                success &= upgradeToVersion4(db, oldVersion);
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
            db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE_NAME);
            onCreate(db);
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }
        return success;
    }

    private boolean upgradeToVersion4(SQLiteDatabase db, int oldVersion) {
        boolean success = true;
        try {
            // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
            // integer MODEL_VERSION to text VERSION
            db.execSQL("DROP TABLE IF EXISTS " + TEMP_FORMS_TABLE_NAME);
            createFormsTableForVersion4(db, TEMP_FORMS_TABLE_NAME);
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
            createFormsTableForVersion4(db, FORMS_TABLE_NAME);
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
        } catch (SQLiteException e) {
            Timber.i(e);
            success = false;
        }

        return success;
    }

    private boolean downgradeToVersion4(SQLiteDatabase db) {
        boolean success = true;
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";

        try {
            QueryBuilder
                    .begin(db)
                    .renameTable(FORMS_TABLE_NAME)
                    .to(temporaryTable)
                    .end();

            createFormsTableForVersion4(db, FORMS_TABLE_NAME);

            QueryBuilder
                    .begin(db)
                    .insertInto(FORMS_TABLE_NAME)
                    .columnsForInsert(formsTableColumnsInVersion4)
                    .select()
                    .columnsForSelect(formsTableColumnsInVersion4)
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

    private void createFormsTableForVersion4(SQLiteDatabase db, String tableName) {
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
}