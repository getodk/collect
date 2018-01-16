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

import org.apache.commons.io.FilenameUtils;

public class FileExtensionUtil {
    private enum FileExtension {
        SVG, UNKNOWN
    }

    private static FileExtension getFileExtension(String filePath) {
        switch (FilenameUtils.getExtension(filePath)) {
            case "svg":
                return FileExtension.SVG;
            default:
                return FileExtension.UNKNOWN;
        }
    }

    public static boolean isSVGFile(String filePath) {
        return FileExtension.SVG.equals(getFileExtension(filePath));
    }
}