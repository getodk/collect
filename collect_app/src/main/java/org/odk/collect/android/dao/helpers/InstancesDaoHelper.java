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
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import timber.log.Timber;

/**
 * Provides abstractions over database calls for instances.
 *
 * @deprecated to favor {@link org.odk.collect.android.instances.InstancesRepository}
 */
@Deprecated
public final class InstancesDaoHelper {

    private InstancesDaoHelper() {

    }

    /**
     * Checks the database to determine if the current instance being edited has
     * already been 'marked completed'. A form can be 'unmarked' complete and
     * then resaved.
     *
     * @return true if form has been marked completed, false otherwise.
     *
     * TODO: replace with method in {@link org.odk.collect.android.instances.InstancesRepository}
     * that returns an {@link Instance} object from a path.
     */
    public static boolean isInstanceComplete(boolean end, boolean completedByDefault) {
        // default to false if we're mid form
        boolean complete = false;

        FormController formController = Collect.getInstance().getFormController();
        if (formController != null && formController.getInstanceFile() != null) {
            // First check if we're at the end of the form, then check the preferences
            complete = end && completedByDefault;

            // Then see if we've already marked this form as complete before
            String path = formController.getInstanceFile().getAbsolutePath();
            try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(InstanceColumns.STATUS);
                    String status = c.getString(columnIndex);
                    if (Instance.STATUS_COMPLETE.equals(status)) {
                        complete = true;
                    }
                }
            }
        } else {
            Timber.w("FormController or its instanceFile field has a null value");
        }
        return complete;
    }

    // TODO: replace with method in {@link org.odk.collect.android.instances.InstancesRepository}
    // that returns an {@link Instance} object from a path.
    public static Uri getLastInstanceUri(String path) {
        if (path != null) {
            try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
                if (c != null && c.getCount() > 0) {
                    // should only be one...
                    c.moveToFirst();
                    String id = c.getString(c.getColumnIndex(InstanceColumns._ID));
                    return Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);
                }
            }
        }
        return null;
    }

    // TODO: replace with method in {@link org.odk.collect.android.instances.InstancesRepository}
    // that returns an {@link Instance} object from a path.
    public static boolean isInstanceAvailable(String path) {
        boolean isAvailable = false;
        if (path != null) {
            try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
                if (c != null) {
                    isAvailable = c.getCount() > 0;
                }
            }
        }
        return isAvailable;
    }
}
