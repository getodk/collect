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
 * Convenience definitions for NotePadProvider
 */
public final class FormsProviderAPI {
    public static final String AUTHORITY = "org.odk.collect.android.provider.odk.forms";

    // This class cannot be instantiated
    private FormsProviderAPI() {}
    
    /**
     * Notes table
     */
    public static final class FormsColumns implements BaseColumns {
        // This class cannot be instantiated
        private FormsColumns() {}


        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forms");
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

        // these are generated for you (but you can insert something else if you want)
        public static final String DISPLAY_SUBTEXT = "displaySubtext";
        public static final String MD5_HASH = "md5Hash";
        public static final String DATE = "date";        
        public static final String JRCACHE_FILE_PATH = "jrcacheFilePath";
        public static final String FORM_MEDIA_PATH = "formMediaPath";      
        
        
        // this is null on create, and can only be set on an update.
        public static final String LANGUAGE = "language";
        
        
    }
}
