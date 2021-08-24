/*
 * Copyright 2019 Nafundi
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FormNameUtils {

    private static final String CONTROL_CHAR_REGEX = "[\\p{Cntrl}]";
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile(CONTROL_CHAR_REGEX);

    private FormNameUtils() {
    }

    public static String normalizeFormName(String formName, boolean returnNullIfNothingChanged) {
        if (formName == null) {
            return null;
        }

        Matcher matcher = CONTROL_CHAR_PATTERN.matcher(formName);
        return matcher.find()
                ? matcher.replaceAll(" ")
                : (returnNullIfNothingChanged ? null : formName);
    }

    // Create a sanitized filename prefix from the given form name. No extension is added.
    public static String formatFilenameFromFormName(String formName) {
        if (formName == null) {
            return null;
        }
        // Keep alphanumerics, replace others with a space
        String fileName = formName.replaceAll("[^\\p{L}\\p{Digit}]", " ");
        // Replace consecutive whitespace characters with single space
        fileName = fileName.replaceAll("\\p{javaWhitespace}+", " ");
        return fileName.trim();
    }
}
