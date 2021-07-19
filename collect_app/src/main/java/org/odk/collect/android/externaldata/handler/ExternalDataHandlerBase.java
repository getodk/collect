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

package org.odk.collect.android.externaldata.handler;

import org.odk.collect.android.externaldata.ExternalDataHandler;
import org.odk.collect.android.externaldata.ExternalDataManager;

import java.util.Locale;

/**
 * Author: Meletis Margaritis
 * Date: 16/05/13
 * Time: 10:42
 */
public abstract class ExternalDataHandlerBase implements ExternalDataHandler {

    private ExternalDataManager externalDataManager;

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }

    protected ExternalDataHandlerBase(ExternalDataManager externalDataManager) {
        this.setExternalDataManager(externalDataManager);
    }

    /**
     * SCTO-545
     *
     * @param dataSetName the user-supplied data-set in the function
     * @return the normalized data-set name.
     */
    protected String normalize(String dataSetName) {
        dataSetName = dataSetName.toLowerCase(Locale.US);
        if (dataSetName.endsWith(".csv")) {
            dataSetName = dataSetName.substring(0, dataSetName.lastIndexOf(".csv"));
        }
        return dataSetName;
    }
}
