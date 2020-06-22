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

import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.PROJECT;       // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.TASKS_ONLY;    // smap
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SOURCE;        // smap

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "forms.db";
    public static final String FORMS_TABLE_NAME = "forms";

    static final int DATABASE_VERSION = 16;     // smap

    private static final String[] COLUMN_NAMES_V7 = {_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            LAST_DETECTED_FORM_VERSION_HASH};

    private static final String[] COLUMN_NAMES_V8 = {_ID, DISPLAY_NAME, DESCRIPTION,
        JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
        SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
        LAST_DETECTED_FORM_VERSION_HASH, GEOMETRY_XPATH};

    private static final String[] COLUMN_NAMES_V16 = {_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            LAST_DETECTED_FORM_VERSION_HASH, GEOMETRY_XPATH,
            PROJECT,    // smap
            TASKS_ONLY, // smap
            SOURCE};    // smap

    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V16;  // smap

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    private static boolean isDatabaseBeingMigrated;

    public FormsDatabaseHelper() {
        super(new DatabaseContext(new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA)), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getDatabasePath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA) + File.separator + DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFormsTableV16(db);    // smap
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            if(oldVersion < 16) {   // smap - start from 16
                upgradeToVersion16(db);
            }

            /* smap
            switch (oldVersion) {
                case 1:
                    upgradeToVersion2(db);
                case 2:
                case 3:
                    upgradeToVersion4(db, oldVersion);
                case 4:
                    upgradeToVersion5(db);
                case 5:
                    upgradeToVersion6(db);
                case 6:
                    upgradeToVersion7(db);
                case 7:
                    upgradeToVersion8(db);
                    break;
                default:
                    Timber.i("Unknown version %d", oldVersion);

            }
             */

            Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }



    // smap sarting point for upgrades
    private void upgradeToVersion16(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_SEND, "text");     // Version 5
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_DELETE, "text");   // Version 7

        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_FORM_VERSION_HASH, "text");

        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, GEOMETRY_XPATH, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, PROJECT, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, TASKS_ONLY, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, SOURCE, "text");

    }
    // smap
    private static void createFormsTableV16(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text,"
                + GEOMETRY_XPATH + " text,"
                + PROJECT + " text,"
                + TASKS_ONLY + " text,"
                + SOURCE + " text,"

                + "displaySubtext text "   // Smap keep for downgrading
                +");");
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
            SQLiteDatabase db = SQLiteDatabase.openDatabase(FormsDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY);
            isDatabaseHelperOutOfDate = FormsDatabaseHelper.DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }
}
