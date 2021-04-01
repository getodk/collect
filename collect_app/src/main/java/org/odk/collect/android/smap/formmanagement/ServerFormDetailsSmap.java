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

package org.odk.collect.android.smap.formmanagement;

import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.utilities.FileUtils;

import java.io.Serializable;

public class ServerFormDetailsSmap implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String formName;
    private final String downloadUrl;
    private final String formID;
    private final String formVersion;
    private final String hash;
    private final boolean isUpdated;
    private final String manifestUrl;
    private boolean isFormDownloaded;
    private boolean tasks_only;
    private String formPath;
    private String project;
    private String formMediaPath;

    private final ManifestFile manifest;

    public ServerFormDetailsSmap(String formName, String downloadUrl, String formID,
                                 String formVersion, String hash,
                                 boolean isUpdated,
                                 ManifestFile manifest,
                                 String manifestUrl,    // smap
                                 boolean isFormDownloaded,
                                 boolean tasks_only,
                                 String formPath,
                                 String project,
                                 String formMediaPath) {   // smap add formNotDownloaded, tasks_only, formPath, project

        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.hash = hash;
        this.isUpdated = isUpdated;
        this.manifestUrl = manifestUrl;
        this.isFormDownloaded = isFormDownloaded;
        this.tasks_only = tasks_only;
        this.formPath = formPath;
        this.project = project;
        this.manifest = manifest;
        this.formMediaPath = formMediaPath;
    }

    public String getFormName() {
        return formName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFormId() {
        return formID;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }           // smap

    public boolean getTasksOnly() {
        return tasks_only;
    }           // smap

    public boolean isFormDownloaded() {
        return isFormDownloaded;
    }           // smap

    public String getFormPath() {
        return formPath;
    }           // smap

    public String getProject() {
        return project;
    }           // smap

    public String getFormMediaPath() {
        return formMediaPath;
    }           // smap

    public String getHash() {
        return hash;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public ManifestFile getManifest() {
        return manifest;
    }
}
