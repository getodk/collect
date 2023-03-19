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

package org.odk.collect.android.external;

import android.net.Uri;

import org.odk.collect.android.database.forms.DatabaseFormColumns;

/**
 * Contract between the forms provider and applications. Contains definitions for the supported
 * URIs. Data columns are defined at {@link DatabaseFormColumns}.
 * <p>
 * This defines the data model for blank forms. Blank forms are unique by
 * {@link DatabaseFormColumns#JR_FORM_ID} unless multiple {@link DatabaseFormColumns#JR_VERSION}s are defined.
 */
public final class FormsContract {

    static final String AUTHORITY = "org.odk.collect.android.provider.odk.forms";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.form";

    /**
     * The content:// style URL for accessing Forms.
     */
    public static Uri getUri(String projectId, Long formDbId) {
        return Uri.parse("content://" + AUTHORITY + "/forms/" + formDbId + "?projectId=" + projectId);
    }

    public static Uri getUri(String projectId) {
        return Uri.parse("content://" + AUTHORITY + "/forms?projectId=" + projectId);
    }

    /**
     * The content:// style URL for accessing the newest versions of Forms. For each
     * {@link DatabaseFormColumns#JR_FORM_ID}, only the version with the most recent
     * {@link DatabaseFormColumns#DATE} is included.
     *
     * @deprecated This should be implemented as part of the UI/app code and should not be
     * available as a special content URI case.
     */
    @Deprecated
    public static Uri getContentNewestFormsByFormIdUri(String projectId) {
        return Uri.parse("content://" + AUTHORITY + "/newest_forms_by_form_id?projectId=" + projectId);
    }

    private FormsContract() {
    }
}
