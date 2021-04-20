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

import org.odk.collect.forms.ManifestFile;

import java.io.Serializable;

public class ServerFormDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String formName;
    private final String downloadUrl;
    private final String formID;
    private final String formVersion;
    private final String hash;
    private final boolean isNotOnDevice;
    private final boolean isUpdated;
    private final ManifestFile manifest;

    public ServerFormDetails(String formName, String downloadUrl, String formID, String formVersion, String hash, boolean isNotOnDevice, boolean isUpdated, ManifestFile manifest) {
        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.hash = hash;
        this.isNotOnDevice = isNotOnDevice;
        this.isUpdated = isUpdated;
        this.manifest = manifest;
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

    public String getHash() {
        return hash;
    }

    public boolean isNotOnDevice() {
        return isNotOnDevice;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public ManifestFile getManifest() {
        return manifest;
    }
}
