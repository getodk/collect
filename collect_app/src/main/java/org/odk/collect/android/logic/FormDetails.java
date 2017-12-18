/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.logic;

import java.io.Serializable;

public class FormDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errorStr;
    private String formName;
    private String downloadUrl;
    private String manifestUrl;
    private String formID;
    private String formVersion;
    private boolean isNewerFormVersionAvailable;
    private boolean areNewerMediaFilesAvailable;
    private boolean tasks_only;    // smap

    public FormDetails(String error) {
        errorStr = error;
        tasks_only = false;     // smap
    }

    public FormDetails(String formName, String downloadUrl, String manifestUrl, String formID,
                       String formVersion, boolean isNewerFormVersionAvailable,
                       boolean areNewerMediaFilesAvailable, boolean tasks_only) {   // smap add tasks_only
        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.manifestUrl = manifestUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.isNewerFormVersionAvailable = isNewerFormVersionAvailable;
        this.areNewerMediaFilesAvailable = areNewerMediaFilesAvailable;
        this.tasks_only = tasks_only;   // smap
    }

    public String getErrorStr() {
        return errorStr;
    }

    public String getFormName() {
        return formName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }

    public String getFormID() {
        return formID;
    }

    public String getFormVersion() {
        return formVersion;
    }

    // smap
    public boolean getTasksOnly() {
        return tasks_only;
    }

    public FormDetails(String name, String url, String manifest, String id, String version, boolean tasks_only) {  // smap add tasks_only
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        formVersion = version;
        errorStr = null;
        this.tasks_only = tasks_only;   // smap
    }
    public boolean isNewerFormVersionAvailable() {
        return isNewerFormVersionAvailable;
    }

    public boolean areNewerMediaFilesAvailable() {
        return areNewerMediaFilesAvailable;
    }
}
