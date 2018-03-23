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

package org.odk.collect.android.utilities;

import java.util.HashMap;
import java.util.Map;

public class MediaManager {

    private static MediaManager singleton;
    
    private Map<String, String> originalFiles;
    private Map<String, String> recentFiles;

    private MediaManager() {
        originalFiles = new HashMap<>();
        recentFiles = new HashMap<>();
    }

    public static MediaManager getInstance() {
        if (singleton == null) {
            singleton = new MediaManager();
        }
        return singleton;
    }

    public void markOriginalFileOrDelete(String index, String fileName) {
        if (originalFiles.containsKey(index)) {
            MediaUtils.deleteImageFileFromMediaProvider(fileName);
        } else {
            originalFiles.put(index, fileName);
        }
    }

    public void markRecentFile(String index, String fileName) {
        if (recentFiles.containsKey(index)) {
            MediaUtils.deleteImageFileFromMediaProvider(recentFiles.get(index));
        }
        recentFiles.put(index, fileName);
    }

    public void revertChanges() {
        for (String fileName : recentFiles.values()) {
            MediaUtils.deleteImageFileFromMediaProvider(fileName);
        }
        releaseMediaManager();
    }

    public void saveChanges() {
        for (String fileName : originalFiles.values()) {
            MediaUtils.deleteImageFileFromMediaProvider(fileName);
        }
        releaseMediaManager();
    }

    private void releaseMediaManager() {
        if (singleton != null) {
            singleton = null;
        }
    }
}
