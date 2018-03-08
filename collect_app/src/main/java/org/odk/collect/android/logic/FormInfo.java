/*
 * Copyright (C) 2018 Shobhit Agarwal
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

public class FormInfo {

    private final String formVersion;
    private final String formID;
    private final String instancePath;

    public FormInfo(String instancePath, String formID, String formVersion) {
        this.instancePath = instancePath;
        this.formID = formID;
        this.formVersion = formVersion;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getFormID() {
        return formID;
    }

    public String getInstancePath() {
        return instancePath;
    }
}
