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

package org.odk.collect.android.utilities;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.HashMap;

public class ApplicationConstants {

    public static final String XML_OPENROSA_NAMESPACE = "http://openrosa.org/xforms";

    // based on http://www.sqlite.org/limits.html
    public static final int SQLITE_MAX_VARIABLE_NUMBER = 999;

    public static final String[] TRANSLATIONS_AVAILABLE = {"af", "am", "ar", "bn",
            "ca", "cs", "de", "en", "es", "et", "fa", "fi", "fr", "ha",
            "hi", "hu", "in", "it", "ja", "ka", "km", "lo_LA", "lt", "mg", "ml", "mr",
            "my", "nb", "ne_NP", "nl", "no", "pl", "ps", "pt", "ro", "ru", "si", "sl", "so",
            "sq", "sv_SE", "sw", "sw_KE", "ta", "th_TH", "ti", "tl", "tr", "uk", "ur",
            "ur_PK", "uz", "vi", "zh", "zu"};

    private ApplicationConstants() {

    }

    public static HashMap<String, Integer> getSortLabelToIconMap() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_name_asc), R.drawable.ic_sort_by_alpha);
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_name_desc), R.drawable.ic_sort_by_alpha);
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_date_asc), R.drawable.ic_access_time);
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_date_desc), R.drawable.ic_access_time);
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_status_asc), R.drawable.ic_assignment_turned_in);
        hashMap.put(Collect.getInstance().getString(R.string.sort_by_status_desc), R.drawable.ic_assignment_late);
        return hashMap;
    }

    public abstract static class BundleKeys {
        public static final String FORM_MODE = "formMode";
        public static final String SUCCESS_KEY = "SUCCESSFUL";
        public static final String ERROR_REASON = "ERROR_MSG";
        public static final String FORM_ID = "FORM_ID";
    }

    public abstract static class FormModes {
        public static final String EDIT_SAVED = "editSaved";
        public static final String VIEW_SENT = "viewSent";
    }

    public abstract static class SortingOrder {
        public static final int BY_NAME_ASC = 0;
        public static final int BY_NAME_DESC = 1;
        public static final int BY_DATE_DESC = 2;
        public static final int BY_DATE_ASC = 3;
        public static final int BY_STATUS_ASC = 4;
        public static final int BY_STATUS_DESC = 5;
    }

    public abstract static class RequestCodes {
        public static final int IMAGE_CAPTURE = 1;
        // public static final int BARCODE_CAPTURE = 2;
        public static final int AUDIO_CAPTURE = 3;
        public static final int VIDEO_CAPTURE = 4;
        public static final int LOCATION_CAPTURE = 5;
        public static final int HIERARCHY_ACTIVITY = 6;
        public static final int IMAGE_CHOOSER = 7;
        public static final int AUDIO_CHOOSER = 8;
        public static final int VIDEO_CHOOSER = 9;
        public static final int EX_STRING_CAPTURE = 10;
        public static final int EX_INT_CAPTURE = 11;
        public static final int EX_DECIMAL_CAPTURE = 12;
        public static final int DRAW_IMAGE = 13;
        public static final int SIGNATURE_CAPTURE = 14;
        public static final int ANNOTATE_IMAGE = 15;
        public static final int ALIGNED_IMAGE = 16;
        public static final int BEARING_CAPTURE = 17;
        public static final int EX_GROUP_CAPTURE = 18;
        public static final int OSM_CAPTURE = 19;
        public static final int GEOSHAPE_CAPTURE = 20;
        public static final int GEOTRACE_CAPTURE = 21;
        public static final int ARBITRARY_FILE_CHOOSER = 22;

        public static final int FORMS_UPLOADED_NOTIFICATION = 97;
        public static final int FORMS_DOWNLOADED_NOTIFICATION = 98;
        public static final int FORM_UPDATES_AVAILABLE_NOTIFICATION = 99;
    }
}
