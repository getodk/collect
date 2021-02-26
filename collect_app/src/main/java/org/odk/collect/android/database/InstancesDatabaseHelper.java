/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of theø License at
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
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import timber.log.Timber;

import static org.odk.collect.android.database.DatabaseConstants.INSTANCES_DATABASE_NAME;
import static org.odk.collect.android.database.DatabaseConstants.INSTANCES_DATABASE_VERSION;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class InstancesDatabaseHelper extends SQLiteOpenHelper {
    private final DatabaseMigrator databaseMigrator;

    public InstancesDatabaseHelper(DatabaseMigrator databaseMigrator, StoragePathProvider storagePathProvider) {
        super(new DatabaseContext(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA)), INSTANCES_DATABASE_NAME, null, INSTANCES_DATABASE_VERSION);
        this.databaseMigrator = databaseMigrator;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        databaseMigrator.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);
        databaseMigrator.onUpgrade(db, oldVersion);
        Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Downgrading database from version %d to %d", oldVersion, newVersion);
        databaseMigrator.onDowngrade(db);
        Timber.i("Downgrading database from version %d to %d completed with success.", oldVersion, newVersion);
    }
}
