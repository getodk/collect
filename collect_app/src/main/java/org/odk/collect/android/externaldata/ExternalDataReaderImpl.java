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

import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.io.FileUtils;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import timber.log.Timber;

/**
 * Author: Meletis Margaritis
 * Date: 30/04/13
 * Time: 09:32
 */
public class ExternalDataReaderImpl implements ExternalDataReader {

    private final FormLoaderTask formLoaderTask;

    public ExternalDataReaderImpl(FormLoaderTask formLoaderTask) {
        this.formLoaderTask = formLoaderTask;
    }

    @Override
    public void doImport(Map<String, File> externalDataMap) {
        for (Map.Entry<String, File> stringFileEntry : externalDataMap.entrySet()) {
            String dataSetName = stringFileEntry.getKey();
            File dataSetFile = stringFileEntry.getValue();
            if (!dataSetFile.exists()) {
                continue;
            }
            if (!doImportDataSetAndContinue(dataSetName, dataSetFile)) {
                return; // halt if import was cancelled
            }
        }
    }

    private boolean doImportDataSetAndContinue(String dataSetName, File dataSetFile) {
        File dbFile = new File(dataSetFile.getParentFile().getAbsolutePath(),
                dataSetName + ".db");
        if (dbFile.exists()) {
            // Determine if we need to reimport
            if (ExternalSQLiteOpenHelper.shouldUpdateDBforDataSet(dbFile, dataSetFile)) {
                boolean deleted = dbFile.delete();
                if (!deleted) {
                    Timber.e("%s has changed but we could not delete the previous DB at %s",
                            dataSetFile.getName(), dbFile.getAbsolutePath());
                    return true;
                }
            } else {
                return true;
            }
        }
        ExternalSQLiteOpenHelper externalSQLiteOpenHelper = new ExternalSQLiteOpenHelper(
                dbFile);
        externalSQLiteOpenHelper.importFromCSV(dataSetFile, this, formLoaderTask);

        if (formLoaderTask != null && formLoaderTask.isCancelled()) {
            Timber.w(
                    "The import was cancelled, so we need to rollback.");

            // we need to drop the database file since it might be partially populated.
            // It will be re-created next time.

            Timber.w("Closing database to be deleted %s", dbFile.toString());

            // then close the database
            SQLiteDatabase db = externalSQLiteOpenHelper.getReadableDatabase();
            db.close();

            // the physically delete the db.
            try {
                FileUtils.forceDelete(dbFile);
                Timber.w("Deleted %s", dbFile.getName());
            } catch (IOException e) {
                Timber.e(e);
            }

            // then just exit and do not process any other CSVs.
            return false;

        }
        return true;
    }

}
