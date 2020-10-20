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

package org.odk.collect.android.formmanagement;

import java.io.Serializable;

import javax.annotation.Nullable;

public class ServerFormDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String formName;
    private final String downloadUrl;
    private final String manifestUrl;
    private final String formID;
    private final String formVersion;
    private final String hash;
    private final String manifestFileHash;
    private final boolean isNotOnDevice;
    private final boolean isUpdated;
    private boolean isFormNotDownloaded;    // smap
    private boolean tasks_only;             // smap
    private String formPath;                // smap
    private String project;                 // smap

    public ServerFormDetails(String formName, String downloadUrl, String manifestUrl, String formID,
                             String formVersion, String hash, String manifestFileHash,
                             boolean isNotOnDevice, boolean isUpdated,
                             boolean isFormNotDownloaded,
                             boolean tasks_only,
                             String formPath,
                             String project) {   // smap add formNotDownloaded, tasks_only, formPath, project
        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.manifestUrl = manifestUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.hash = hash;
        this.manifestFileHash = manifestFileHash;
        this.isNotOnDevice = isNotOnDevice;
        this.isUpdated = isUpdated;
        this.isFormNotDownloaded = isFormNotDownloaded;   // smap
        this.tasks_only = tasks_only;   // smap
        this.formPath = formPath;       // smap
        this.project = project;       // smap
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

    public String getFormId() {
        return formID;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public boolean getTasksOnly() {
        return tasks_only;
    }           // smap

    public boolean isFormNotDownloaded() {
        return isFormNotDownloaded;
    }           // smap

    public String getFormPath() {
        return formPath;
    }           // smap

    public String getProject() {
        return project;
    }           // smap

    public String getHash() {
        return hash;
    }

    @Nullable
    public String getManifestFileHash() {
        return manifestFileHash;
    }

    public boolean isNotOnDevice() {
        return isNotOnDevice;
    }

    public boolean isUpdated() {
        return isUpdated;
    }
}
