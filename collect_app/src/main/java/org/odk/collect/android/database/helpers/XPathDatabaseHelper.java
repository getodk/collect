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
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.XPathProviderAPI;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class XPathDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "xpath_exprs.db";
    public static final String XPATH_TABLE_NAME = "xpath_exprs";
    public static final String INDEXED_EXPRESIONS = "indexed_expressions";

    private static final int DATABASE_VERSION = 7;


    public XPathDatabaseHelper() {
        super(new DatabaseContext(Collect.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createXPathTableV7(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

        Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(XPATH_TABLE_NAME)
                .end();

        createXPathTableV7(db);

        Timber.i("Downgrading database from %d to %d completed with success.", oldVersion, newVersion);
    }

    private void createXPathTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + XPATH_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + XPathProviderAPI.XPathsColumns.EVAL_EXPR + " text not null, "
                + XPathProviderAPI.XPathsColumns.TREE_REF + " text );");
    }

}
