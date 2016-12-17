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

package org.odk.collect.android.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Trace database
 */
public final class TraceProviderAPI {
    public static final String AUTHORITY = "org.smap.smapTask.android.provider.trace";

    // This class cannot be instantiated
    private TraceProviderAPI() {}


    public static final class TraceColumns implements BaseColumns {
        // This class cannot be instantiated
        private TraceColumns() {}
        
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/trace");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.trace";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.trace";

        public static final String SOURCE = "source";
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String TIME = "time";

    }
}
