/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.tasks;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.WebUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploader extends InstanceUploader {

    // it can take up to 27 seconds to spin up Aggregate
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final String URL_PATH_SEP = "/";



    private boolean processChunk(int low, int high, WebUtils.Outcome outcome, Long... values) {
        if (values == null) {
            // don't try anything if values is null
            return false;
        }

        StringBuilder selectionBuf = new StringBuilder(InstanceColumns._ID + " IN (");
        String[] selectionArgs = new String[high - low];
        for (int i = 0; i < (high - low); i++) {
            if (i > 0) {
                selectionBuf.append(",");
            }
            selectionBuf.append("?");
            selectionArgs[i] = values[i + low].toString();
        }

        selectionBuf.append(")");
        String selection = selectionBuf.toString();

        String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));


        Map<Uri, Uri> uriRemap = new HashMap<>();

        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursor(selection, selectionArgs);

            if (c != null && c.getCount() > 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if (isCancelled()) {
                        return false;
                    }

                    publishProgress(c.getPosition() + 1 + low, values.length);
                    String instance = c.getString(
                            c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                    String id = c.getString(c.getColumnIndex(InstanceColumns._ID));
                    Uri toUpdate = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);

                    // Use the app's configured URL unless the form included a submission URL
                    int subIdx = c.getColumnIndex(InstanceColumns.SUBMISSION_URI);
                    String urlString = c.isNull(subIdx)
                            ? getServerSubmissionURL() : c.getString(subIdx).trim();

                    // add the deviceID to the request...
                    try {
                        urlString += "?deviceID=" + URLEncoder.encode(deviceId, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // unreachable...
                        Timber.i(e, "Error encoding URL for device id : %s", deviceId);
                    }

                    Collect.getInstance().getActivityLogger().logAction(this, urlString, instance);

                    if (!WebUtils.uploadFile(urlString, id, instance, toUpdate, uriRemap, outcome)) {
                        return false; // get credentials...
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return true;
    }

    @Override

    protected WebUtils.Outcome doInBackground(Long... values) {
        WebUtils.Outcome outcome = new WebUtils.Outcome();
        int counter = 0;
        while (counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER < values.length) {
            int low = counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            int high = (counter + 1) * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
            if (high > values.length) {
                high = values.length;
            }
            if (!processChunk(low, high, outcome, values)) {
                return outcome;
            }
            counter++;
        }
        return outcome;
    }

    private String getServerSubmissionURL() {

        Collect app = Collect.getInstance();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                Collect.getInstance());
        String serverBase = settings.getString(PreferenceKeys.KEY_SERVER_URL,
                app.getString(R.string.default_server_url));

        if (serverBase.endsWith(URL_PATH_SEP)) {
            serverBase = serverBase.substring(0, serverBase.length() - 1);
        }

        // NOTE: /submission must not be translated! It is the well-known path on the server.
        String submissionPath = settings.getString(PreferenceKeys.KEY_SUBMISSION_URL,
                app.getString(R.string.default_odk_submission));

        if (!submissionPath.startsWith(URL_PATH_SEP)) {
            submissionPath = URL_PATH_SEP + submissionPath;
        }

        return serverBase + submissionPath;
    }

}
