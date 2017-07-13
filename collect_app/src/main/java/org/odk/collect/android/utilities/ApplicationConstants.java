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

public class ApplicationConstants {

    // based on http://www.sqlite.org/limits.html
    public static final int SQLITE_MAX_VARIABLE_NUMBER = 999;

    public static final String[] TRANSLATIONS_AVAILABLE = {"af", "am", "ar", "bn",
            "ca", "cs", "de", "en", "es", "et", "fa", "fi", "fr", "ha",
            "hi", "hu", "in", "it", "ja", "ka", "km", "lo_LA", "lt", "mg", "mr",
            "my", "nb", "ne_NP", "nl", "no", "pl", "ps", "pt", "ro", "ru", "so",
            "sq", "sw", "sw_KE", "ta", "th_TH", "tl", "tr", "uk", "ur",
            "ur_PK", "vi", "zh", "zu"};

    public abstract static class BundleKeys {
        public static final String FORM_MODE = "formMode";
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
}
