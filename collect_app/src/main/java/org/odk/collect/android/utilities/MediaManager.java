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

/**
 * This class is responsible for managing media files collected by users. The purpose here is to
 * remember an original media file answer (no matter how many times this answer is replaced), in order
 * to be able to restore the original answer in case of ignoring changes.
 */
public enum MediaManager {
    INSTANCE;

    Map<String, String> originalFiles = new HashMap<>();
    Map<String, String> recentFiles = new HashMap<>();

    public void markOriginalFileOrDelete(String questionIndex, String fileName) {
        if (questionIndex != null && fileName != null) {
            if (originalFiles.containsKey(questionIndex)) {
                MediaUtils.deleteImageFileFromMediaProvider(fileName);
            } else {
                originalFiles.put(questionIndex, fileName);
            }
        }
    }

    public void replaceRecentFileForQuestion(String questionIndex, String fileName) {
        if (questionIndex != null && fileName != null) {
            if (recentFiles.containsKey(questionIndex)) {
                MediaUtils.deleteImageFileFromMediaProvider(recentFiles.get(questionIndex));
            }
            recentFiles.put(questionIndex, fileName);
        }
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
        originalFiles.clear();
        recentFiles.clear();
    }
}
