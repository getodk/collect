/*
 * Copyright (C) 2017 University of Washington
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

package org.odk.collect.android.utilities;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.BadUrlException;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

public final class UrlUtils {

    private UrlUtils() {

    }

    @NonNull
    public static String getSpreadsheetID(String urlString) throws BadUrlException {
        // now parse the url string if we have one
        final String googleHeader = "docs.google.com/spreadsheets/d/";
        if (urlString == null || urlString.isEmpty()) {
            throw new BadUrlException(
                    getLocalizedString(Collect.getInstance(), R.string.missing_submission_url));
        } else if (urlString.length() < googleHeader.length()) {
            throw new BadUrlException(
                    getLocalizedString(Collect.getInstance(), R.string.invalid_sheet_id, urlString));
        } else {
            int start = urlString.indexOf(googleHeader) + googleHeader.length();
            int end = urlString.indexOf('/', start);
            if (end == -1) {
                // if there wasn't a "/", just try to get the end
                end = urlString.length();
            }
            if (start == -1) {
                throw new BadUrlException(
                        getLocalizedString(Collect.getInstance(), R.string.invalid_sheet_id, urlString));
            }
            return urlString.substring(start, end);
        }
    }
}
