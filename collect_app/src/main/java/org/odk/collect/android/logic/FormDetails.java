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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class FormDetails implements Parcelable {
    private static final long serialVersionUID = 1L;

    private String errorStr;
    private String formName;
    private String downloadUrl;
    private String manifestUrl;
    private String formID;
    private String formVersion;
    private boolean isNewerFormVersionAvailable;
    private boolean areNewerMediaFilesAvailable;

    public FormDetails(String error) {
        errorStr = error;
    }

    public FormDetails(String formName, String downloadUrl, String manifestUrl, String formID,
                       String formVersion, boolean isNewerFormVersionAvailable,
                       boolean areNewerMediaFilesAvailable) {
        this.formName = formName;
        this.downloadUrl = downloadUrl;
        this.manifestUrl = manifestUrl;
        this.formID = formID;
        this.formVersion = formVersion;
        this.isNewerFormVersionAvailable = isNewerFormVersionAvailable;
        this.areNewerMediaFilesAvailable = areNewerMediaFilesAvailable;
    }

    protected FormDetails(Parcel in) {
        errorStr = in.readString();
        formName = in.readString();
        downloadUrl = in.readString();
        manifestUrl = in.readString();
        formID = in.readString();
        formVersion = in.readString();
        isNewerFormVersionAvailable = in.readByte() != 0;
        areNewerMediaFilesAvailable = in.readByte() != 0;
    }

    public static final Creator<FormDetails> CREATOR = new Creator<FormDetails>() {
        @Override
        public FormDetails createFromParcel(Parcel in) {
            return new FormDetails(in);
        }

        @Override
        public FormDetails[] newArray(int size) {
            return new FormDetails[size];
        }
    };

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

    public boolean isNewerFormVersionAvailable() {
        return isNewerFormVersionAvailable;
    }

    public boolean areNewerMediaFilesAvailable() {
        return areNewerMediaFilesAvailable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(errorStr);
        parcel.writeString(formName);
        parcel.writeString(downloadUrl);
        parcel.writeString(manifestUrl);
        parcel.writeString(formID);
        parcel.writeString(formVersion);
        parcel.writeByte((byte) (isNewerFormVersionAvailable ? 1 : 0));
        parcel.writeByte((byte) (areNewerMediaFilesAvailable ? 1 : 0));
    }
}
