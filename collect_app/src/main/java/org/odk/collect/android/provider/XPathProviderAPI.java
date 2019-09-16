package org.odk.collect.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class XPathProviderAPI {
    static final String AUTHORITY = "org.odk.collect.android.provider.odk.xpath_expr_index";

    private XPathProviderAPI() {
    }

    /**
     * Columns for the XPathExpressionIndex table.
     */
    public static final class XPathsColumns implements BaseColumns {
        private XPathsColumns() {
        }

        /**
         * The content:// style URL for accessing XPaths.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/xpath_expr_index");


        // These are the only things needed indexing a leaf node which has a value
        public static final String EVAL_EXPR = "evaluationExpression";
        public static final String GENERIC_TREE_REF = "treeReference";
        public static final String SPECIFIC_TREE_REF_ = "treeReference";

    }
}
