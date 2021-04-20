/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.forms;

public class MediaFile {
    private final String filename;
    private final String hash;
    private final String downloadUrl;

    public MediaFile(String filename, String hash, String downloadUrl) {
        this.filename = filename;
        this.hash = hash;
        this.downloadUrl = downloadUrl;
    }

    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
