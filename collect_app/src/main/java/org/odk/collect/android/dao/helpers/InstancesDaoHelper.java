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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.ArrayList;
import java.util.List;

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
                    .get(GeneralKeys.KEY_COMPLETED_DEFAULT);

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
        if (path != null) {
            try (Cursor c = new InstancesDao().getInstancesCursorForFilePath(path)) {
                if (c != null && c.getCount() > 0) {
                    // should only be one...
                    c.moveToFirst();
                    String id = c.getString(c.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                    return Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, id);
                }
            }
        }
        return null;
    }

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

    /**
     * Returns all instances available through the cursor and closes the cursor.
     */
    public static List<Instance> getInstancesFromCursor(Cursor cursor) {
        List<Instance> instances = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
                    int submissionUriColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI);
                    int canEditWhenCompleteIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
                    int instanceFilePathIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);
                    int statusColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
                    int lastStatusChangeDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE);
                    int deletedDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE);

                    int databaseIdIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);

                    Instance instance = new Instance.Builder()
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .canEditWhenComplete(cursor.getString(canEditWhenCompleteIndex))
                            .instanceFilePath(cursor.getString(instanceFilePathIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .status(cursor.getString(statusColumnIndex))
                            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
                            .deletedDate(cursor.getLong(deletedDateColumnIndex))
                            .databaseId(cursor.getLong(databaseIdIndex))
                            .build();

                    instances.add(instance);
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }

    /**
     * Returns the values of an instance as a ContentValues object for use with
     * {@link org.odk.collect.android.dao.InstancesDao#saveInstance(ContentValues)} (ContentValues)}
     * or {@link org.odk.collect.android.dao.InstancesDao#updateInstance(ContentValues, String, String[])}
     *
     * Does NOT include the database ID.
     */
    public static ContentValues getValuesFromInstanceObject(Instance instance) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, instance.getDisplayName());
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, instance.getSubmissionUri());
        values.put(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, instance.getCanEditWhenComplete());
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instance.getInstanceFilePath());
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, instance.getJrFormId());
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, instance.getJrVersion());
        values.put(InstanceProviderAPI.InstanceColumns.STATUS, instance.getStatus());
        values.put(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, instance.getLastStatusChangeDate());
        values.put(InstanceProviderAPI.InstanceColumns.DELETED_DATE, instance.getDeletedDate());

        return values;
    }
}
