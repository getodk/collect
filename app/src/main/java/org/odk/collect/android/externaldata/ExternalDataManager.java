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

/**
 * This class handles all DB connections for the function handlers
 * <p/>
 * Author: Meletis Margaritis
 * Date: 14/05/13
 * Time: 17:17
 */
public interface ExternalDataManager {

    /**
     * Returns an object of {@link ExternalSQLiteOpenHelper}
     *
     * @param dataSetName the name of the imported .csv
     * @param required    if true, a runtime exception
     *                    ({@link org.odk.collect.android.exception.ExternalDataException})
     *                    will be thrown.
     *                    if false null will be returned
     * @return an object of {@link ExternalSQLiteOpenHelper}
     */
    ExternalSQLiteOpenHelper getDatabase(String dataSetName, boolean required);

    void close();
}
