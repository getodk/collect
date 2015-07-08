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

import android.util.Log;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalDataException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Meletis Margaritis
 * Date: 14/05/13
 * Time: 17:19
 */
public class ExternalDataManagerImpl implements ExternalDataManager {

    private Map<String, ExternalSQLiteOpenHelper> dbMap = new HashMap<String, ExternalSQLiteOpenHelper>();

    private final File mediaFolder;

    public ExternalDataManagerImpl(File mediaFolder) {
        this.mediaFolder = mediaFolder;
    }

    @Override
    public ExternalSQLiteOpenHelper getDatabase(String dataSetName, boolean required) {
        ExternalSQLiteOpenHelper sqLiteOpenHelper = dbMap.get(dataSetName);
        if (sqLiteOpenHelper == null) {
            if (mediaFolder == null) {
                String msg = Collect.getInstance().getString(R.string.ext_not_initialized_error);
                Log.e(ExternalDataUtil.LOGGER_NAME, msg);
                if (required) {
                    throw new ExternalDataException(msg);
                } else {
                    return null;
                }
            } else {
                File dbFile = new File(mediaFolder, dataSetName + ".db");
                if (!dbFile.exists()) {
                    String msg = Collect.getInstance().getString(R.string.ext_import_csv_missing_error, dataSetName, dataSetName);
                    Log.e(ExternalDataUtil.LOGGER_NAME, msg);
                    if (required) {
                        throw new ExternalDataException(msg);
                    } else {
                        return null;
                    }
                } else {
                    sqLiteOpenHelper = new ExternalSQLiteOpenHelper(dbFile);
                    dbMap.put(dataSetName, sqLiteOpenHelper);
                }
            }
        }
        return sqLiteOpenHelper;
    }

    @Override
    public void close() {
        if (dbMap != null) {
            for (ExternalSQLiteOpenHelper externalSQLiteOpenHelper : dbMap.values()) {
                Log.w(ExternalDataUtil.LOGGER_NAME, "Closing database handler:" + externalSQLiteOpenHelper.toString());
                externalSQLiteOpenHelper.close();
            }
        }
    }
}
