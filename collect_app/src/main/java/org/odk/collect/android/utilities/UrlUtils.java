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

import android.database.Cursor;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.BadUrlException;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.regex.Pattern;

public class UrlUtils {

    public static boolean isValidUrl(String url) {

        final Pattern urlPattern = Pattern.compile("^https?:\\/\\/.+$", Pattern.CASE_INSENSITIVE);

        return urlPattern.matcher(url).matches();
    }

    public static String getSpreadsheetID(String id)
            throws BadUrlException {
        Cursor cursor = null;
        String urlString = null;
        try {
            // see if the submission element was defined in the form
            cursor = new InstancesDao().getInstancesCursorForId(id);

            if (cursor.getCount() > 0) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int subIdx = cursor.getColumnIndex(
                            InstanceProviderAPI.InstanceColumns.SUBMISSION_URI);
                    urlString = cursor.isNull(subIdx) ? null : cursor.getString(subIdx);

                    // if we didn't find one in the content provider,
                    // try to get from settings
                    if (urlString == null) {
                        urlString = (String) GeneralSharedPreferences.getInstance()
                                .get(PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // now parse the url string if we have one
        final String googleHeader = "docs.google.com/spreadsheets/d/";
        String spreadsheetId;
        if (urlString == null || urlString.length() < googleHeader.length()) {
            throw new BadUrlException(
                    Collect.getInstance().getString(R.string.invalid_sheet_id, urlString));
        } else {
            int start = urlString.indexOf(googleHeader) + googleHeader.length();
            int end = urlString.indexOf("/", start);
            if (end == -1) {
                // if there wasn't a "/", just try to get the end
                end = urlString.length();
            }
            if (start == -1 || end == -1) {
                throw new BadUrlException(
                        Collect.getInstance().getString(R.string.invalid_sheet_id, urlString));
            }
            spreadsheetId = urlString.substring(start, end);
            return spreadsheetId;
        }
    }


}
