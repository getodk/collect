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

package org.odk.collect.android.externaldata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.AltDatabasePathContext;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;
import org.odk.collect.android.utilities.CustomSQLiteQueryExecutor;
import org.odk.collect.android.utilities.SQLiteUtils;
import org.odk.collect.shared.strings.Md5;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

/**
 * Author: Meletis Margaritis
 * Date: 30/04/13
 * Time: 09:36
 */
public class ExternalSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final char DELIMITING_CHAR = ",".charAt(0);
    private static final char QUOTE_CHAR = "\"".charAt(0);
    private static final char ESCAPE_CHAR = "\0".charAt(0);

    private File dataSetFile;
    private ExternalDataReader externalDataReader;
    private FormLoaderTask formLoaderTask;

    ExternalSQLiteOpenHelper(File dbFile) {
        super(new AltDatabasePathContext(dbFile.getParentFile().getAbsolutePath(), Collect.getInstance()), dbFile.getName(), null, VERSION);
    }

    void importFromCSV(File dataSetFile, ExternalDataReader externalDataReader,
                       FormLoaderTask formLoaderTask) {
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
            // this means that the function handler needed the database through calling
            // getReadableDatabase() --> getWritableDatabase(),
            // but this is not allowed, so just return;
            Timber.e("The function handler triggered this external data population. This is not good.");
            return;
        }

        try {
            if (shouldUpdateDBforDataSet(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, dataSetFile)) {
                onCreateNamed(db, ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME);
                createAndPopulateMetadataTable(db, ExternalDataUtil.EXTERNAL_METADATA_TABLE_NAME, dataSetFile);
            }
        } catch (Exception e) {
            throw new ExternalDataException(
                    getLocalizedString(Collect.getInstance(), R.string.ext_import_generic_error,
                            dataSetFile.getName(), e.getMessage()), e);
        }
    }

    private void onCreateNamed(SQLiteDatabase db, String tableName) throws Exception {
        Timber.w("Reading data from '%s", dataSetFile.toString());

        onProgress(getLocalizedString(Collect.getInstance(), R.string.ext_import_progress_message,
                dataSetFile.getName(), ""));

        CSVReader reader = null;
        try {
            reader = new CSVReaderBuilder(new FileReader(dataSetFile))
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(DELIMITING_CHAR)
                            .withQuoteChar(QUOTE_CHAR)
                            .withEscapeChar(ESCAPE_CHAR)
                            .build())
                    .build();
            String[] headerRow = reader.readNext();

            headerRow[0] = removeByteOrderMark(headerRow[0]);

            if (!ExternalDataUtil.containsAnyData(headerRow)) {
                throw new ExternalDataException(
                        getLocalizedString(Collect.getInstance(), R.string.ext_file_no_data_error));
            }

            List<String> conflictingColumns =
                    ExternalDataUtil.findMatchingColumnsAfterSafeningNames(headerRow);

            if (conflictingColumns != null && !conflictingColumns.isEmpty()) {
                // this means that after removing invalid characters, some column names resulted
                // with the same name,
                // so the create table query will fail with "duplicate column" error.
                throw new ExternalDataException(
                        getLocalizedString(Collect.getInstance(), R.string.ext_conflicting_columns_error,
                                conflictingColumns));
            }

            Map<String, String> columnNamesCache = new HashMap<>();

            StringBuilder sb = new StringBuilder();

            boolean sortColumnAlreadyPresent = false;

            sb
                    .append("CREATE TABLE IF NOT EXISTS ")
                    .append(tableName)
                    .append(" ( ");

            for (int i = 0; i < headerRow.length; i++) {
                String columnName = headerRow[i].trim();
                if (columnName.length() == 0) {
                    continue;
                }
                if (i != 0) {
                    sb.append(", ");
                }
                String safeColumnName = ExternalDataUtil.toSafeColumnName(columnName,
                        columnNamesCache);
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

            Timber.w("Creating database for %s with query: %s", dataSetFile, sql);
            db.execSQL(sql);

            // create the indexes.
            // save the sql for later because inserts will be much faster if we don't have
            // indexes already.
            List<String> createIndexesCommands = new ArrayList<>();
            for (String header : headerRow) {
                if (header.endsWith("_key")) {
                    String indexSQL = "CREATE INDEX " + header + "_idx ON " + tableName + " ("
                            + ExternalDataUtil.toSafeColumnName(header, columnNamesCache) + ");";
                    createIndexesCommands.add(indexSQL);
                    Timber.w("Will create an index on %s later.", header);
                }
            }

            // populate the database
            String[] row = reader.readNext();
            int rowCount = 0;
            while (row != null && !isCancelled()) {
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
                    String safeColumnName = ExternalDataUtil.toSafeColumnName(columnName,
                            columnNamesCache);
                    if (safeColumnName.equals(ExternalDataUtil.SORT_COLUMN_NAME)) {
                        try {
                            values.put(safeColumnName, Double.parseDouble(columnValue));
                        } catch (NumberFormatException e) {
                            throw new ExternalDataException(getLocalizedString(Collect.getInstance(), R.string.ext_sortBy_numeric_error, columnValue));
                        }
                    } else {
                        values.put(safeColumnName, columnValue);
                    }
                }
                db.insertOrThrow(tableName, null, values);
                row = reader.readNext();
                rowCount++;
                if (rowCount % 100 == 0) {
                    onProgress(getLocalizedString(Collect.getInstance(), R.string.ext_import_progress_message,
                            dataSetFile.getName(), " (" + rowCount + " records so far)"));
                }
            }

            if (isCancelled()) {
                Timber.w("User canceled reading data from %s", dataSetFile.toString());
                onProgress(getLocalizedString(Collect.getInstance(), R.string.ext_import_cancelled_message));
            } else {

                onProgress(getLocalizedString(Collect.getInstance(), R.string.ext_import_finalizing_message));

                // now create the indexes
                for (String createIndexCommand : createIndexesCommands) {
                    Timber.w(createIndexCommand);
                    db.execSQL(createIndexCommand);
                }

                Timber.w("Read all data from %s", dataSetFile.toString());
                onProgress(getLocalizedString(Collect.getInstance(), R.string.ext_import_completed_message));
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
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
        metadata.put(ExternalDataUtil.COLUMN_MD5_HASH, Md5.getMd5Hash(dataSetFile));
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
        String newFileMd5 = Md5.getMd5Hash(dataSetFile);
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
