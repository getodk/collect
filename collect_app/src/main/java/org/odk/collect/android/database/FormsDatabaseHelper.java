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

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.database.DatabaseConstants.FORMS_DATABASE_VERSION;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "forms.db";

    private static boolean isDatabaseBeingMigrated;

    private final FormDatabaseMigrator formDatabaseMigrator;

    public FormsDatabaseHelper() {
        super(new DatabaseContext(new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA)), DATABASE_NAME, null, FORMS_DATABASE_VERSION);
        formDatabaseMigrator = new FormDatabaseMigrator();
    }

    public static String getDatabasePath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA) + File.separator + DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        formDatabaseMigrator.onCreate(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            formDatabaseMigrator.onUpgrade(db, oldVersion);

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
            formDatabaseMigrator.onDowngrade(db);

            Timber.i("Downgrading database from %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
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
            isDatabaseHelperOutOfDate = FORMS_DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }
}
