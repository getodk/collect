/*
 * Copyright (C) 2018 Nafundi
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

package org.odk.collect.android.upload;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class InstanceUploader {
    public static final String FAIL = "Error: ";

    /**
     * Uploads the specified instance to the specified destination URL. It may return a custom
     * success message on completion or null if none is available. Errors result in an UploadException.
     *
     * Updates the database status for the instance.
     */
    @Nullable
    public abstract String uploadOneSubmission(Instance instance, String destinationUrl) throws UploadException;

    @NonNull
    public abstract String getUrlToSubmitTo(Instance currentInstance, String deviceId, String overrideURL, String urlFromSettings);

    /**
     * Returns a list of Instance objects corresponding to the database IDs passed in.
     */
    public List<Instance> getInstancesFromIds(Long... instanceDatabaseIds) {
        List<Instance> instancesToUpload = new ArrayList<>();
        InstancesDao dao = new InstancesDao();

        // Split the queries to avoid exceeding SQLITE_MAX_VARIABLE_NUMBER
        int counter = 0;
        while (counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER < instanceDatabaseIds.length) {
            int low = counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            int high = (counter + 1) * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            if (high > instanceDatabaseIds.length) {
                high = instanceDatabaseIds.length;
            }

            StringBuilder selectionBuf = new StringBuilder(InstanceColumns._ID + " IN (");
            String[] selectionArgs = new String[high - low];
            for (int i = 0; i < (high - low); i++) {
                if (i > 0) {
                    selectionBuf.append(',');
                }
                selectionBuf.append('?');
                selectionArgs[i] = instanceDatabaseIds[i + low].toString();
            }

            selectionBuf.append(')');
            String selection = selectionBuf.toString();

            Cursor c = dao.getInstancesCursor(selection, selectionArgs);
            instancesToUpload.addAll(dao.getInstancesFromCursor(c));

            counter++;
        }

        return instancesToUpload;
    }

    public void saveSuccessStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                instance.getId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceColumns.STATUS, Instance.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
    }

    public void saveFailedStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                instance.getId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceColumns.STATUS, Instance.STATUS_SUBMISSION_FAILED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
    }
}
