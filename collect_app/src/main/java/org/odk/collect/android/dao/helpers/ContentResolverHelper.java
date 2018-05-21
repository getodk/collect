/*
 * Copyright (C) 2018 Shobhit Agarwal
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

package org.odk.collect.android.dao.helpers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

public final class ContentResolverHelper {

    private ContentResolverHelper() {

    }

    public static FormInfo getFormDetails(Uri uri) {
        FormInfo formInfo = null;

        ContentResolver contentResolver = Collect.getInstance().getContentResolver();

        try (Cursor instanceCursor = contentResolver.query(uri, null, null, null, null)) {
            if (instanceCursor != null && instanceCursor.getCount() > 0) {
                instanceCursor.moveToFirst();
                String instancePath = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(
                                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

                String jrFormId = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                int idxJrVersion = instanceCursor
                        .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);

                //String jrVersion = instanceCursor.isNull(idxJrVersion) ? null     // smap
                //        : instanceCursor                                          // smap
                //        .getString(idxJrVersion);                                 // smap
                formInfo = new FormInfo(instancePath, jrFormId, null);  // smap set jrVersion null - always use latest version

                formInfo.source = instanceCursor.getString(instanceCursor.getColumnIndex(FormsProviderAPI.FormsColumns.SOURCE));                // smap
                formInfo.name = instanceCursor.getString(instanceCursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));            // smap
                formInfo.submissionUri = instanceCursor.getString(instanceCursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI)); // smap
            }
        }
        return formInfo;
    }

    public static String getFormPath(Uri uri) {
        String formPath = null;
        ContentResolver contentResolver = Collect.getInstance().getContentResolver();
        try (Cursor c = contentResolver.query(uri, null, null, null, null)) {
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                formPath = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
            }
        }
        return formPath;
    }
}
