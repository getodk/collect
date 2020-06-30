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

import org.odk.collect.android.openrosa.api.FormListItem;

import java.io.Serializable;
import java.util.Objects;

public class FormDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errorStr;
    private String formName;
    private String downloadUrl;
    private String manifestUrl;
    private String formID;
    private String formVersion;
    private String hash;
    private String manifestFileHash;
    private boolean isNewerFormVersionAvailable;
    private boolean areNewerMediaFilesAvailable;

    public FormDetails(String error) {
        errorStr = error;
    }

    public FormDetails(String formName, String downloadUrl, String manifestUrl, String formID,
                       String formVersion, String hash, String manifestFileHash,
                       boolean isNewerFormVersionAvailable, boolean areNewerMediaFilesAvailable) {
        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.manifestUrl = manifestUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.hash = hash;
        this.manifestFileHash = manifestFileHash;
        this.isNewerFormVersionAvailable = isNewerFormVersionAvailable;
        this.areNewerMediaFilesAvailable = areNewerMediaFilesAvailable;
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

    public String getFormId() {
        return formID;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getHash() {
        return hash;
    }

    public String getManifestFileHash() {
        return manifestFileHash;
    }

    public boolean isNewerFormVersionAvailable() {
        return isNewerFormVersionAvailable;
    }

    public boolean areNewerMediaFilesAvailable() {
        return areNewerMediaFilesAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormDetails that = (FormDetails) o;
        return isNewerFormVersionAvailable() == that.isNewerFormVersionAvailable() &&
                areNewerMediaFilesAvailable == that.areNewerMediaFilesAvailable &&
                Objects.equals(getErrorStr(), that.getErrorStr()) &&
                Objects.equals(getFormName(), that.getFormName()) &&
                Objects.equals(getDownloadUrl(), that.getDownloadUrl()) &&
                Objects.equals(getManifestUrl(), that.getManifestUrl()) &&
                Objects.equals(formID, that.formID) &&
                Objects.equals(getFormVersion(), that.getFormVersion()) &&
                Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getManifestFileHash(), that.getManifestFileHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getErrorStr(), getFormName(), getDownloadUrl(), getManifestUrl(), formID, getFormVersion(), getHash(), getManifestFileHash(), isNewerFormVersionAvailable(), areNewerMediaFilesAvailable);
    }

    public static FormDetails toFormDetails(FormListItem formListItem) {
        return toFormDetails(formListItem, null, false, false);
    }

    public static FormDetails toFormDetails(FormListItem formListItem, String manifestFileHash, boolean isNewerFormVersionAvailable, boolean areNewerMediaFilesAvailable) {
        return new FormDetails(
                formListItem.getName(),
                formListItem.getDownloadURL(),
                formListItem.getManifestURL(),
                formListItem.getFormID(),
                formListItem.getVersion(),
                formListItem.getHashWithPrefix(),
                manifestFileHash,
                isNewerFormVersionAvailable,
                areNewerMediaFilesAvailable
        );
    }
}
