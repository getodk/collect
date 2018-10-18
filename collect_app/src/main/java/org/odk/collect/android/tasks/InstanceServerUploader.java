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

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.http.CollectServerClient.Outcome;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ResponseMessageParser;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploader extends InstanceUploader {

    private static final String URL_PATH_SEP = "/";

    private static final String FAIL = "Error: ";

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    // Custom submission URL, username and password that can be sent via intent extras by external
    // applications
    private String completeDestinationUrl;
    private String customUsername;
    private String customPassword;

    public InstanceServerUploader() {
        Collect.getInstance().getComponent().inject(this);
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

            if (!uploadOneSubmission(instance, urlString, uriRemap, outcome)) {
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

    /**
     * Uploads all files associated with an instance to the specified URL.
     *
     * @return false if credentials are required and we should terminate immediately.
     */
    private boolean uploadOneSubmission(Instance instance, String urlString, Map<Uri, Uri> uriRemap,
                                        Outcome outcome) {
        Uri submissionUri = Uri.parse(urlString);

        boolean openRosaServer = false;
        if (uriRemap.containsKey(submissionUri)) {
            // we already issued a head request and got a response,
            // so we know the proper URL to send the submission to
            // and the proper scheme. We also know that it was an
            // OpenRosa compliant server.
            openRosaServer = true;
            submissionUri = uriRemap.get(submissionUri);
            Timber.i("Using Uri remap for submission %s. Now: %s", instance.getDatabaseId(),
                    submissionUri.toString());
        } else {
            if (submissionUri.getHost() == null) {
                Timber.i("Host name may not be null");
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        FAIL + "Host name may not be null");
                saveFailedStatusToDatabase(instance);
                return true;
            }

            URI uri;
            try {
                uri = URI.create(submissionUri.toString());
            } catch (IllegalArgumentException e) {
                Timber.i(e);
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), Collect.getInstance().getString(R.string.url_error));
                return false;
            }

            try {
                HttpHeadResult headResult = httpInterface.head(uri, webCredentialsUtils.getCredentials(uri));
                Map<String, String> responseHeaders = headResult.getHeaders();

                if (headResult.getStatusCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    outcome.authRequestingServer = submissionUri;
                    return false;
                } else if (headResult.getStatusCode() == HttpsURLConnection.HTTP_NO_CONTENT) {
                    if (responseHeaders.containsKey("Location")) {
                        try {
                            Uri newURI = Uri.parse(URLDecoder.decode(responseHeaders.get("Location"), "utf-8"));
                            if (submissionUri.getHost().equalsIgnoreCase(newURI.getHost())) {
                                openRosaServer = true;
                                // trust the server to tell us a new location
                                // ... and possibly to use https instead.
                                // Re-add params if server didn't respond with params
                                if (newURI.getQuery() == null) {
                                    newURI = newURI.buildUpon()
                                            .encodedQuery(submissionUri.getEncodedQuery())
                                            .build();
                                }
                                uriRemap.put(submissionUri, newURI);
                                submissionUri = newURI;
                            } else {
                                // Don't follow a redirection attempt to a different host.
                                // We can't tell if this is a spoof or not.
                                outcome.messagesByInstanceId.put(
                                        instance.getDatabaseId().toString(),
                                        FAIL
                                                + "Unexpected redirection attempt to a different "
                                                + "host: "
                                                + newURI.toString());
                                saveFailedStatusToDatabase(instance);
                                return true;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Exception thrown parsing URI for url %s", urlString);
                            outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                                    FAIL + urlString + " " + e.toString());
                            saveFailedStatusToDatabase(instance);
                            return true;
                        }
                    }

                } else {
                    Timber.w("Status code on Head request: %d", headResult.getStatusCode());
                    if (headResult.getStatusCode() >= HttpsURLConnection.HTTP_OK && headResult.getStatusCode() < HttpsURLConnection.HTTP_MULT_CHOICE) {
                        outcome.messagesByInstanceId.put(
                                instance.getDatabaseId().toString(),
                                FAIL
                                        + "Invalid status code on Head request.  If you have a "
                                        + "web proxy, you may need to login to your network. ");
                        saveFailedStatusToDatabase(instance);
                        return true;
                    }
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }

                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + msg);
                Timber.e(e);
                saveFailedStatusToDatabase(instance);
                return true;
            }
        }

        // At this point, we may have updated the uri to use https.
        // This occurs only if the Location header keeps the host name
        // the same. If it specifies a different host name, we error
        // out.
        //
        // And we may have set authentication cookies in our
        // cookiestore (referenced by localContext) that will enable
        // authenticated publication to the server.
        //
        // get instance file

        // Under normal operations, we upload the instanceFile to
        // the server.  However, during the save, there is a failure
        // window that may mark the submission as complete but leave
        // the file-to-be-uploaded with the name "submission.xml" and
        // the plaintext submission files on disk.  In this case,
        // upload the submission.xml and all the files in the directory.
        // This means the plaintext files and the encrypted files
        // will be sent to the server and the server will have to
        // figure out what to do with them.

        File instanceFile = new File(instance.getInstanceFilePath());
        File submissionFile = new File(instanceFile.getParentFile(), "submission.xml");
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + "instance XML file does not exist!");
            saveFailedStatusToDatabase(instance);
            return true;
        }

        List<File> files = getFilesInParentDirectory(instanceFile, submissionFile, openRosaServer);

        if (files == null) {
            return false;
        }

        ResponseMessageParser messageParser;

        try {
            URI uri = URI.create(submissionUri.toString());

            messageParser = httpInterface.uploadSubmissionFile(files, submissionFile, uri,
                    webCredentialsUtils.getCredentials(uri));

            int responseCode = messageParser.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_CREATED && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + "Network login failure? Again?");
                } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
                } else {
                    // If response from server is valid use that else use default messaging
                    if (messageParser.isValid()) {
                        outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + messageParser.getMessageResponse());
                    } else {
                        outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
                    }

                }
                saveFailedStatusToDatabase(instance);
                return true;
            }

        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), FAIL + "Generic Exception: " + msg);
            saveFailedStatusToDatabase(instance);
            return true;
        }

        // If response from server is valid use that else use default messaging
        if (messageParser.isValid()) {
            outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), messageParser.getMessageResponse());
        } else {
            // Default messaging
            outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), Collect.getInstance().getString(R.string.success));
        }

        saveSuccessStatusToDatabase(instance);

        Collect.getInstance()
                .getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("Submission")
                        .setAction("HTTP")
                        .build());

        return true;
    }

    private List<File> getFilesInParentDirectory(File instanceFile, File submissionFile, boolean openRosaServer) {
        List<File> files = new ArrayList<>();

        // find all files in parent directory
        File[] allFiles = instanceFile.getParentFile().listFiles();
        if (allFiles == null) {
            return null;
        }

        for (File f : allFiles) {
            String fileName = f.getName();

            if (fileName.startsWith(".")) {
                continue; // ignore invisible files
            } else if (fileName.equals(instanceFile.getName())) {
                continue; // the xml file has already been added
            } else if (fileName.equals(submissionFile.getName())) {
                continue; // the xml file has already been added
            }

            String extension = FileUtils.getFileExtension(fileName);

            if (openRosaServer) {
                files.add(f);
            } else if (extension.equals("jpg")) { // legacy 0.9x
                files.add(f);
            } else if (extension.equals("3gpp")) { // legacy 0.9x
                files.add(f);
            } else if (extension.equals("3gp")) { // legacy 0.9x
                files.add(f);
            } else if (extension.equals("mp4")) { // legacy 0.9x
                files.add(f);
            } else if (extension.equals("osm")) { // legacy 0.9x
                files.add(f);
            } else {
                Timber.w("unrecognized file type %s", f.getName());
            }
        }

        return files;
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

    private void saveSuccessStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                instance.getDatabaseId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
    }

    private void saveFailedStatusToDatabase(Instance instance) {
        Uri instanceDatabaseUri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                instance.getDatabaseId().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
        Collect.getInstance().getContentResolver().update(instanceDatabaseUri, contentValues, null, null);
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
