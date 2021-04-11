/*
 * Copyright 2018 Nafundi
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

import java.util.List;

public class ManifestFile {
    private final String hash;
    private final List<MediaFile> mediaFiles;

    public ManifestFile(String hash, List<MediaFile> mediaFiles) {
        this.hash = hash;
        this.mediaFiles = mediaFiles;
    }

    public String getHash() {
        return hash;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }
}
