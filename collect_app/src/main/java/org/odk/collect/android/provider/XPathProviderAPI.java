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

public final class XPathProviderAPI {
    static final String AUTHORITY = "org.odk.collect.android.provider.odk.xpath";

    private XPathProviderAPI() {
    }

    /**
     * Columns for the XPaths table.
     */
    public static final class XPathsColumns implements BaseColumns {
        private XPathsColumns() {
        }

        /**
         * The content:// style URL for accessing XPaths.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/xpath");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.xpath";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.xpath";

        // These are the only things needed for an insert
        public static final String PRE_EVAL_EXPR = "preEvaluatedExpression";
        public static final String TREE_REF = "treeReference";

    }
}
