/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.smap.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.database.SmapReferenceDatabaseHelper;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataReader;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.CustomSQLiteQueryExecutor;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.SQLiteUtils;
import org.odk.collect.android.utilities.TranslationHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import timber.log.Timber;

import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;

/**
 * Author: Smap Consulting
 * Date: 22/03/2021
 */
public class LocalSQLiteOpenHelperSmap extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final char DELIMITING_CHAR = ",".charAt(0);
    private static final char QUOTE_CHAR = "\"".charAt(0);
    private static final char ESCAPE_CHAR = "\0".charAt(0);

    private FormLoaderTask formLoaderTask;

    public LocalSQLiteOpenHelperSmap(File dbFile) {
        super(new DatabaseContext(dbFile.getParentFile().getAbsolutePath()), dbFile.getName(), null, VERSION);
    }

    // Open the database
    public void append(ArrayList<ContentValues> data, FormLoaderTask formLoaderTask) throws java.lang.Exception {
        this.formLoaderTask = formLoaderTask;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();

            // make sure the local column exists - it may not if the user has just upgraded from an older version of fieldTask
            SQLiteUtils.addColumn(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, ExternalDataUtil.LOCAL_COLUMN_NAME, "integer");

            // Delete existing local data
            String selection = ExternalDataUtil.LOCAL_COLUMN_NAME + " = 1";
            db.delete(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, selection, null);

            appendLocal(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, data);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    // Database should already be created
    public void onCreate(SQLiteDatabase db) {
        throw new ExternalDataException(
                TranslationHandler.getString(Collect.getInstance(), R.string.smap_local_data));
    }

    private void appendLocal(SQLiteDatabase db, String tableName, ArrayList<ContentValues> data) throws Exception {

        onProgress(TranslationHandler.getString(Collect.getInstance(), R.string.smap_local_data));

        for (ContentValues values : data) {
            values.put(ExternalDataUtil.LOCAL_COLUMN_NAME, 1);    // Set local indicator
            db.insertOrThrow(tableName, null, values);
        }
    }

    protected boolean isCancelled() {
        return formLoaderTask != null && formLoaderTask.isCancelled();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void onProgress(String message) {
        if (formLoaderTask != null) {
            formLoaderTask.publishExternalDataLoadingProgress(message);
        }
    }

}
