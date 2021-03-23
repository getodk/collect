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

/**
 * Author: Smap Consulting
 * Date: 22/03/2021
 */
public class LocalSQLiteOpenHelperSmap extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final char DELIMITING_CHAR = ",".charAt(0);
    private static final char QUOTE_CHAR = "\"".charAt(0);
    private static final char ESCAPE_CHAR = "\0".charAt(0);

    private ArrayList<ContentValues> data;
    private ExternalDataReader externalDataReader;
    private FormLoaderTask formLoaderTask;

    public LocalSQLiteOpenHelperSmap(File dbFile) {
        super(new DatabaseContext(dbFile.getParentFile().getAbsolutePath()), dbFile.getName(), null, VERSION);
    }

    // Open the database
    public void append(ArrayList<ContentValues> data, FormLoaderTask formLoaderTask) throws java.lang.Exception {
        this.data = data;
        this.formLoaderTask = formLoaderTask;

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            appendLocal(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME);
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

    private void appendLocal(SQLiteDatabase db, String tableName) throws Exception {

        onProgress(TranslationHandler.getString(Collect.getInstance(), R.string.smap_local_data));

        Map<String, String> columnNamesCache = new HashMap<>();

        StringBuilder sb = new StringBuilder();

        boolean sortColumnAlreadyPresent = false;

        for (ContentValues values : data) {
            db.insertOrThrow(tableName, null, values);
        }
    }

    protected boolean isCancelled() {
        return formLoaderTask != null && formLoaderTask.isCancelled();
    }

    // Create a metadata table with a single column that keeps track of the date of the last import
    // of this data set.
    static void createAndPopulateMetadataTable(SQLiteDatabase db, String metadataTableName, File dataSetFile) {
        final String dataSetFilenameColumn = CustomSQLiteQueryBuilder.quoteIdentifier(ExternalDataUtil.COLUMN_DATASET_FILENAME);
        final String md5HashColumn = CustomSQLiteQueryBuilder.quoteIdentifier(ExternalDataUtil.COLUMN_MD5_HASH);

        List<String> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(CustomSQLiteQueryBuilder.formatColumnDefinition(dataSetFilenameColumn, "TEXT"));
        columnDefinitions.add(CustomSQLiteQueryBuilder.formatColumnDefinition(md5HashColumn, "TEXT NOT NULL"));

        CustomSQLiteQueryExecutor.begin(db).createTable(metadataTableName).columnsForCreate(columnDefinitions).end();

        ContentValues metadata = new ContentValues();
        metadata.put(ExternalDataUtil.COLUMN_DATASET_FILENAME, dataSetFile.getName());
        metadata.put(ExternalDataUtil.COLUMN_MD5_HASH, FileUtils.getMd5Hash(dataSetFile));
        db.insertOrThrow(metadataTableName, null, metadata);
    }

    static String getLastMd5Hash(SQLiteDatabase db, String metadataTableName, File dataSetFile) {
        final String dataSetFilenameColumn = CustomSQLiteQueryBuilder.quoteIdentifier(ExternalDataUtil.COLUMN_DATASET_FILENAME);
        final String md5HashColumn = CustomSQLiteQueryBuilder.quoteIdentifier(ExternalDataUtil.COLUMN_MD5_HASH);
        final String dataSetFilenameLiteral = CustomSQLiteQueryBuilder.quoteStringLiteral(dataSetFile.getName());

        String[] columns = {md5HashColumn};
        String selectionCriteria = CustomSQLiteQueryBuilder.formatCompareEquals(dataSetFilenameColumn, dataSetFilenameLiteral);
        Cursor cursor = db.query(metadataTableName, columns, selectionCriteria, null, null, null, null);

        String lastImportMd5 = "";
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            lastImportMd5 = cursor.getString(0);
        }
        cursor.close();
        return lastImportMd5;
    }

    static boolean shouldUpdateDBforDataSet(File dbFile, File dataSetFile) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        return shouldUpdateDBforDataSet(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, dataSetFile);
    }

    static boolean shouldUpdateDBforDataSet(SQLiteDatabase db, String dataTableName, String metadataTableName, File dataSetFile) {
        if (!SQLiteUtils.doesTableExist(db, dataTableName)) {
            return true;
        }
        if (!SQLiteUtils.doesTableExist(db, metadataTableName)) {
            return true;
        }
        // Import if the CSV file has been updated
        String priorImportMd5 = getLastMd5Hash(db, metadataTableName, dataSetFile);
        String newFileMd5 = FileUtils.getMd5Hash(dataSetFile);
        return newFileMd5 == null || !newFileMd5.equals(priorImportMd5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void onProgress(String message) {
        if (formLoaderTask != null) {
            formLoaderTask.publishExternalDataLoadingProgress(message);
        }
    }

    /**
     * Removes a Byte Order Mark (BOM) from the start of a String.
     *
     * @param bomCheckString is checked to see if it starts with a Byte Order Mark.
     * @return bomCheckString without a Byte Order Mark.
     */
    private String removeByteOrderMark(String bomCheckString) {
        return bomCheckString.startsWith("\uFEFF") ? bomCheckString.substring(1) : bomCheckString;
    }
}
