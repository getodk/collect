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
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.http.CollectServerClient.Outcome;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.upload.InstanceServerUploader;
import org.odk.collect.android.upload.result.SubmissionUploadAuthRequested;
import org.odk.collect.android.upload.result.SubmissionUploadResult;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploaderTask extends InstanceUploaderTask {
    private static final String URL_PATH_SEP = "/";

    private final InstanceServerUploader uploader;

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    // Custom submission URL, username and password that can be sent via intent extras by external
    // applications
    private String completeDestinationUrl;
    private String customUsername;
    private String customPassword;

    public InstanceServerUploaderTask() {
        Collect.getInstance().getComponent().inject(this);
        uploader = new InstanceServerUploader(httpInterface, webCredentialsUtils);
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        Outcome outcome = new Outcome();

        List<Instance> instancesToUpload = getInstancesFromIds(instanceIdsToUpload);

        String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));

        Map<Uri, Uri> uriRemap = new HashMap<>();

        for (int i = 0; i < instancesToUpload.size(); i++) {
            if (isCancelled()) {
                return outcome;
            }
            Instance instance = instancesToUpload.get(i);

            publishProgress(i + 1, instancesToUpload.size());

            String urlString = getURLToSubmitTo(instance, deviceId, completeDestinationUrl);

            SubmissionUploadResult result = uploader.uploadOneSubmission(instance, urlString, uriRemap);

            // Don't add the instance that caused an auth request to the map because we want to
            // retry; items in the map are not reconsidered in this submission attempt
            if (result instanceof SubmissionUploadAuthRequested) {
                outcome.authRequestingServer = ((SubmissionUploadAuthRequested) result).getAuthRequestingServerUri();
            } else {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), result.getDisplayMessage());
            }

            if (result.isFatalError()) {
                return outcome;
            }
        }
        
        return outcome;
    }

    /**
     * Returns a list of Instance objects corresponding to the database IDs passed in.
     */
    private List<Instance> getInstancesFromIds(Long... instanceDatabaseIds) {
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

    /**
     * Returns the URL this instance should be submitted to with appended deviceId.
     *
     * If the upload was triggered by an external app and specified an override URL, use that one.
     * Otherwise, use the submission URL configured in the form
     * (https://opendatakit.github.io/xforms-spec/#submission-attributes). Finally, default to the
     * URL configured at the app level.
     */
    @NonNull
    private String getURLToSubmitTo(Instance currentInstance, String deviceId, String overrideURL) {
        String urlString;

        if (overrideURL != null) {
            urlString = overrideURL;
        } else if (currentInstance.getSubmissionUri() != null) {
            urlString = currentInstance.getSubmissionUri().trim();
        } else {
            urlString = getServerSubmissionURL();
        }

        // add deviceID to request
        try {
            urlString += "?deviceID=" + URLEncoder.encode(deviceId != null ? deviceId : "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.i(e, "Error encoding URL for device id : %s", deviceId);
        }

        return urlString;
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

    @Override
    protected void onPostExecute(Outcome outcome) {
        super.onPostExecute(outcome);

        // Clear temp credentials
        clearTemporaryCredentials();
    }

    @Override
    protected void onCancelled() {
        clearTemporaryCredentials();
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl) {
        setCompleteDestinationUrl(completeDestinationUrl, true);
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl, boolean clearPreviousConfig) {
        this.completeDestinationUrl = completeDestinationUrl;
        if (clearPreviousConfig) {
            setTemporaryCredentials();
        }
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
        setTemporaryCredentials();
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
        setTemporaryCredentials();
    }

    private void setTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.saveCredentials(completeDestinationUrl, customUsername, customPassword);
        } else {
            // In the case for anonymous logins, clear the previous credentials for that host
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }

    private void clearTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }
}