/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract between the forms provider and applications. Contains definitions for the supported URIs
 * and data columns.
 *
 * This defines the data model for blank forms. Blank forms are unique by
 * {@link FormsColumns#JR_FORM_ID} unless multiple {@link FormsColumns#JR_VERSION}s are defined.
 */
public final class FormsProviderAPI {
    static final String AUTHORITY = "org.odk.collect.android.provider.odk.forms";

    private FormsProviderAPI() {
    }

    /**
     * Columns for the Forms table.
     */
    public static final class FormsColumns implements BaseColumns {
        private FormsColumns() {
        }

        /**
         * The content:// style URL for accessing Forms.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forms");

        /**
         * The content:// style URL for accessing the newest versions of Forms. For each
         * {@link FormsColumns#JR_FORM_ID}, only the version with the most recent
         * {@link FormsColumns#DATE} is included.
         */
        public static final Uri CONTENT_NEWEST_FORMS_BY_FORMID_URI = Uri.parse("content://" + AUTHORITY + "/newest_forms_by_form_id");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.form";

        // These are the only things needed for an insert
        public static final String DISPLAY_NAME = "displayName";
        public static final String DESCRIPTION = "description";  // can be null
        public static final String JR_FORM_ID = "jrFormId";
        public static final String JR_VERSION = "jrVersion"; // can be null
        public static final String FORM_FILE_PATH = "formFilePath";
        public static final String SUBMISSION_URI = "submissionUri"; // can be null
        public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey"; // can be null
        public static final String AUTO_DELETE = "autoDelete"; // can be null
        public static final String LAST_DETECTED_FORM_VERSION_HASH = "lastDetectedFormVersionHash"; // can be null
        // Column is called autoSubmit for legacy support but the attribute is auto-send
        public static final String AUTO_SEND = "autoSubmit"; // can be null

        // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext"; // not used in the newest database version
        public static final String MD5_HASH = "md5Hash";
        public static final String DATE = "date";
        public static final String MAX_DATE = "MAX(date)"; // used only to get latest forms for each form_id
        public static final String JRCACHE_FILE_PATH = "jrcacheFilePath";
        public static final String FORM_MEDIA_PATH = "formMediaPath";

        // this is null on create, and can only be set on an update.
        public static final String LANGUAGE = "language";
    }
}
