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

import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;

import timber.log.Timber;

public final class InstancesDaoHelper {

    private InstancesDaoHelper() {

    }

    /**
     * Checks the database to determine if the current instance being edited has
     * already been 'marked completed'. A form can be 'unmarked' complete and
     * then resaved.
     *
     * @return true if form has been marked completed, false otherwise.
     */
    public static boolean isInstanceComplete(boolean end) {
        // default to false if we're mid form
        boolean complete = false;

        FormController formController = Collect.getInstance().getFormController();
        if (formController != null && formController.getInstanceFile() != null) {
            // First check if we're at the end of the form, then check the preferences
            complete = end && (boolean) GeneralSharedPreferences.getInstance()
                    .get(PreferenceKeys.KEY_COMPLETED_DEFAULT);

            // Then see if we've already marked this form as complete before
            String path = formController.getInstanceFile().getAbsolutePath();
            try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
                    String status = c.getString(columnIndex);
                    if (InstanceProviderAPI.STATUS_COMPLETE.equals(status)) {
                        complete = true;
                    }
                }
            }
        } else {
            Timber.w("FormController or its instanceFile field has a null value");
        }
        return complete;
    }

    public static Uri getLastInstanceUri(String path) {
        try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
            if (c != null && c.getCount() > 0) {
                // should only be one...
                c.moveToFirst();
                String id = c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                return Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, id);
            }
        }
        return null;
    }

    public static boolean isInstanceAvailable(String path) {
        boolean isAvailable = false;
        try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
            if (c != null) {
                isAvailable = c.getCount() > 0;
            }
        }
        return isAvailable;
    }
}
