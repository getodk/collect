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

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.utilities.TranslationHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Author: Meletis Margaritis
 * Date: 14/05/13
 * Time: 17:19
 */
public class ExternalDataManagerImpl implements ExternalDataManager {

    private final Map<String, ExternalSQLiteOpenHelper> dbMap = new HashMap<>();

    private final File mediaFolder;

    public ExternalDataManagerImpl(File mediaFolder) {
        this.mediaFolder = mediaFolder;
    }

    @Override
    public ExternalSQLiteOpenHelper getDatabase(String dataSetName, boolean required) {
        ExternalSQLiteOpenHelper sqLiteOpenHelper = dbMap.get(dataSetName);
        if (sqLiteOpenHelper == null) {
            if (mediaFolder == null) {
                String msg = TranslationHandler.getString(Collect.getInstance(), R.string.ext_not_initialized_error);
                Timber.e(msg);
                if (required) {
                    throw new ExternalDataException(msg);
                } else {
                    return null;
                }
            } else {
                sqLiteOpenHelper = new ExternalSQLiteOpenHelper(new File(mediaFolder, dataSetName + ".db"));
                dbMap.put(dataSetName, sqLiteOpenHelper);
            }
        }
        return sqLiteOpenHelper;
    }

    @Override
    public void close() {
        if (dbMap != null) {
            for (ExternalSQLiteOpenHelper externalSQLiteOpenHelper : dbMap.values()) {
                Timber.w("Closing database handler:%s", externalSQLiteOpenHelper.toString());
                externalSQLiteOpenHelper.close();
            }
        }
    }
}
