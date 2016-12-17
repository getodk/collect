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

package org.odk.collect.android.external;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Meletis Margaritis
 * Date: 30/04/13
 * Time: 09:36
 */
public class ExternalSQLiteOpenHelper extends ODKSQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final char DELIMITING_CHAR = ",".charAt(0);
    private static final char QUOTE_CHAR = "\"".charAt(0);
    private static final char ESCAPE_CHAR = "\0".charAt(0);

    private File dataSetFile;
    private ExternalDataReader externalDataReader;
    private FormLoaderTask formLoaderTask;

    public ExternalSQLiteOpenHelper(File dbFile) {
        super(dbFile.getParentFile().getAbsolutePath(), dbFile.getName(), null, VERSION);
    }

    public void importFromCSV(File dataSetFile, ExternalDataReader externalDataReader, FormLoaderTask formLoaderTask) {
        this.dataSetFile = dataSetFile;
        this.externalDataReader = externalDataReader;
        this.formLoaderTask = formLoaderTask;

        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
        } finally {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (externalDataReader == null) {
            // this means that the function handler needed the database through calling getReadableDatabase() --> getWritableDatabase(),
            // but this is not allowed, so just return;
            Log.e(ExternalDataUtil.LOGGER_NAME, "The function handler triggered this external data population. This is not good.");
            return;
        }

        try {
            onCreateNamed(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME);
        } catch (Exception e) {
            throw new ExternalDataException(Collect.getInstance().getString(R.string.ext_import_generic_error, dataSetFile.getName(), e.getMessage()), e);
        }
    }

    private void onCreateNamed(SQLiteDatabase db, String tableName) throws Exception {
        Log.w(ExternalDataUtil.LOGGER_NAME, "Reading data from '" + dataSetFile);

        onProgress(Collect.getInstance().getString(R.string.ext_import_progress_message, dataSetFile.getName(), ""));

        CSVReader reader = null;
        try {
            reader = new CSVReader(new InputStreamReader(new FileInputStream(dataSetFile), "UTF-8"), DELIMITING_CHAR, QUOTE_CHAR, ESCAPE_CHAR);
            String[] headerRow = reader.readNext();

            if (!ExternalDataUtil.containsAnyData(headerRow)) {
                throw new ExternalDataException(Collect.getInstance().getString(R.string.ext_file_no_data_error));
            }

            List<String> conflictingColumns = ExternalDataUtil.findMatchingColumnsAfterSafeningNames(headerRow);

            if (conflictingColumns != null && conflictingColumns.size() > 0) {
                // this means that after removing invalid characters, some column names resulted with the same name,
                // so the create table query will fail with "duplicate column" error.
                throw new ExternalDataException(Collect.getInstance().getString(R.string.ext_conflicting_columns_error, conflictingColumns));
            }

            Map<String, String> columnNamesCache = new HashMap<String, String>();

            StringBuilder sb = new StringBuilder();

            boolean sortColumnAlreadyPresent = false;

            sb.append("CREATE TABLE ");
            sb.append(tableName);
            sb.append(" ( ");
            for (int i = 0; i < headerRow.length; i++) {
                String columnName = headerRow[i].trim();
                if (columnName.length() == 0) {
                    continue;
                }
                if (i != 0) {
                    sb.append(", ");
                }
                String safeColumnName = ExternalDataUtil.toSafeColumnName(columnName, columnNamesCache);
                if (safeColumnName.equals(ExternalDataUtil.SORT_COLUMN_NAME)) {
                    sortColumnAlreadyPresent = true;
                    sb.append(safeColumnName).append(" real ");
                } else {
                    sb.append(safeColumnName).append(" text collate nocase ");
                }
            }
            if (!sortColumnAlreadyPresent) {
                sb.append(", ");
                sb.append(ExternalDataUtil.SORT_COLUMN_NAME).append(" real ");
            }

            sb.append(" );");
            String sql = sb.toString();

            Log.w(ExternalDataUtil.LOGGER_NAME, "Creating database for " + dataSetFile + " with query: " + sql);
            db.execSQL(sql);

            // create the indexes.
            // save the sql for later because inserts will be much faster if we don't have indexes already.
            List<String> createIndexesCommands = new ArrayList<String>();
            for (String header : headerRow) {
                if (header.endsWith("_key")) {
                    String indexSQL = "CREATE INDEX " + header + "_idx ON " + tableName + " (" + ExternalDataUtil.toSafeColumnName(header, columnNamesCache) + ");";
                    createIndexesCommands.add(indexSQL);
                    Log.w(ExternalDataUtil.LOGGER_NAME, "Will create an index on " + header + " later.");
                }
            }

            // populate the database
            String[] row = reader.readNext();
            int rowCount = 0;
            while (row != null && !formLoaderTask.isCancelled()) {
                // SCTO-894 - first we should make sure that this is not an empty line
                if (!ExternalDataUtil.containsAnyData(row)) {
                    // yes, that is an empty row, ignore it
                    row = reader.readNext();
                    continue;
                }

                // SCTO-894 - then check if the row contains less values than the header
                // we should not ignore the existing values in the row,
                // we will just fill up the rest with empty strings
                if (row.length < headerRow.length) {
                    row = ExternalDataUtil.fillUpNullValues(row, headerRow);
                }

                ContentValues values = new ContentValues();
                if (!sortColumnAlreadyPresent) {
                    values.put(ExternalDataUtil.SORT_COLUMN_NAME, rowCount + 1);
                }

                for (int i = 0; i < row.length && i < headerRow.length; i++) {
                    String columnName = headerRow[i].trim();
                    String columnValue = row[i];
                    if (columnName.length() == 0) {
                        continue;
                    }
                    String safeColumnName = ExternalDataUtil.toSafeColumnName(columnName, columnNamesCache);
                    if (safeColumnName.equals(ExternalDataUtil.SORT_COLUMN_NAME)) {
                        try {
                            values.put(safeColumnName, Double.parseDouble(columnValue));
                        } catch (NumberFormatException e) {
                            throw new ExternalDataException(Collect.getInstance().getString(R.string.ext_sortBy_numeric_error, columnValue));
                        }
                    } else {
                        values.put(safeColumnName, columnValue);
                    }
                }
                db.insertOrThrow(tableName, null, values);
                row = reader.readNext();
                rowCount++;
                if (rowCount % 100 == 0) {
                    onProgress(Collect.getInstance().getString(R.string.ext_import_progress_message, dataSetFile.getName(), " (" + rowCount + " records so far)"));
                }
            }

            if (formLoaderTask.isCancelled()) {
                Log.w(ExternalDataUtil.LOGGER_NAME, "User canceled reading data from " + dataSetFile);
                onProgress(Collect.getInstance().getString(R.string.ext_import_cancelled_message));
            } else {

                onProgress(Collect.getInstance().getString(R.string.ext_import_finalizing_message));

                // now create the indexes
                for (String createIndexCommand : createIndexesCommands) {
                    Log.w(ExternalDataUtil.LOGGER_NAME, createIndexCommand);
                    db.execSQL(createIndexCommand);
                }

                Log.w(ExternalDataUtil.LOGGER_NAME, "Read all data from " + dataSetFile);
                onProgress(Collect.getInstance().getString(R.string.ext_import_completed_message));
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(ExternalDataUtil.LOGGER_NAME, e.getMessage(), e);
                }
            }
        }
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
