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

public final class InstancesContract {

    public static final String AUTHORITY = "org.odk.collect.android.provider.odk.instances";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.instance";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.instance";

    public static Uri getUri(String projectId) {
        return Uri.parse("content://" + AUTHORITY + "/instances?projectId=" + projectId);
    }

    public static Uri getUri(String projectId, Long instanceDbId) {
        return Uri.parse("content://" + AUTHORITY + "/instances/" + instanceDbId + "?projectId=" + projectId);
    }

    // This class cannot be instantiated
    private InstancesContract() {
    }

}
