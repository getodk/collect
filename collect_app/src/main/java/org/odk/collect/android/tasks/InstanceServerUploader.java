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

import com.google.android.gms.analytics.HitBuilders;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
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

    private String completeDestinationUrl;
    private String customUsername;
    private String customPassword;

    public InstanceServerUploader() {
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    protected Outcome doInBackground(Long... values) {
        Outcome outcome = new Outcome();
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

    private boolean processChunk(int low, int high, Outcome outcome, Long... values) {
        if (values == null) {
            // don't try anything if values is null
            return false;
        }

        StringBuilder selectionBuf = new StringBuilder(InstanceColumns._ID + " IN (");
        String[] selectionArgs = new String[high - low];
        for (int i = 0; i < (high - low); i++) {
            if (i > 0) {
                selectionBuf.append(',');
            }
            selectionBuf.append('?');
            selectionArgs[i] = values[i + low].toString();
        }

        selectionBuf.append(')');
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

                    /*
                     Submission url precedence/priority:
                      * Intent submission url
                      * Form submission URL
                      * The configured URL in the app settings
                    */
                    int subIdx = c.getColumnIndex(InstanceColumns.SUBMISSION_URI);
                    String urlString = completeDestinationUrl != null ? completeDestinationUrl : c.isNull(subIdx)
                            ? getServerSubmissionURL() : c.getString(subIdx).trim();

                    // add the deviceID to the request...
                    try {
                        urlString += "?deviceID=" + URLEncoder.encode(deviceId != null ? deviceId : "", "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // unreachable...
                        Timber.i(e, "Error encoding URL for device id : %s", deviceId);
                    }

                    if (!uploadSubmissionFile(urlString, id, instance, toUpdate, uriRemap, outcome)) {
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

    /**
     * Uploads a submission file to a url
     *
     * @param urlString - The Destination URL
     * @param id - Form ID
     * @param instanceFilePath - path + filename to upload
     * @param toUpdate - Content Provider URI to update
     * @param uriRemap - Map of uri's that are to be re-mapped to a submission URI
     * @param outcome - An object to hold the results of the file upload
     * @return false if credentials are required and we should terminate immediately.
     */
    private boolean uploadSubmissionFile(String urlString, String id, String instanceFilePath,
                                         Uri toUpdate,
                                         Map<Uri, Uri> uriRemap,
                                         Outcome outcome) {

        ContentValues contentValues = new ContentValues();
        Uri submissionUri = Uri.parse(urlString);

        boolean openRosaServer = false;
        if (uriRemap.containsKey(submissionUri)) {
            // we already issued a head request and got a response,
            // so we know the proper URL to send the submission to
            // and the proper scheme. We also know that it was an
            // OpenRosa compliant server.
            openRosaServer = true;
            submissionUri = uriRemap.get(submissionUri);
            Timber.i("Using Uri remap for submission %s. Now: %s", id, submissionUri.toString());
        } else {
            if (submissionUri.getHost() == null) {
                Timber.i("Host name may not be null");
                outcome.messagesByInstanceId.put(id, FAIL + "Host name may not be null");
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
                return true;
            }

            URI uri;
            try {
                uri = URI.create(submissionUri.toString());
            } catch (IllegalArgumentException e) {
                Timber.i(e);
                outcome.messagesByInstanceId.put(id, Collect.getInstance().getString(R.string.url_error));
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
                                        id,
                                        FAIL
                                                + "Unexpected redirection attempt to a different "
                                                + "host: "
                                                + newURI.toString());
                                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS,
                                        InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                                Collect.getInstance().getContentResolver()
                                        .update(toUpdate, contentValues, null, null);
                                return true;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Exception thrown parsing URI for url %s", urlString);
                            outcome.messagesByInstanceId.put(id, FAIL + urlString + " " + e.toString());
                            contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS,
                                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                            Collect.getInstance().getContentResolver()
                                    .update(toUpdate, contentValues, null, null);
                            return true;
                        }
                    }

                } else {
                    Timber.w("Status code on Head request: %d", headResult.getStatusCode());
                    if (headResult.getStatusCode() >= HttpsURLConnection.HTTP_OK && headResult.getStatusCode() < HttpsURLConnection.HTTP_MULT_CHOICE) {
                        outcome.messagesByInstanceId.put(
                                id,
                                FAIL
                                        + "Invalid status code on Head request.  If you have a "
                                        + "web proxy, you may need to login to your network. ");
                        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS,
                                InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                        Collect.getInstance().getContentResolver()
                                .update(toUpdate, contentValues, null, null);
                        return true;
                    }
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }

                outcome.messagesByInstanceId.put(id, FAIL + msg);
                Timber.e(e);
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
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

        File instanceFile = new File(instanceFilePath);
        File submissionFile = new File(instanceFile.getParentFile(), "submission.xml");
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            outcome.messagesByInstanceId.put(id, FAIL + "instance XML file does not exist!");
            contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
            Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
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
                    outcome.messagesByInstanceId.put(id, FAIL + "Network login failure? Again?");
                } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    outcome.messagesByInstanceId.put(id, FAIL + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
                } else {
                    // If response from server is valid use that else use default messaging
                    if (messageParser.isValid()) {
                        outcome.messagesByInstanceId.put(id, FAIL + messageParser.getMessageResponse());
                    } else {
                        outcome.messagesByInstanceId.put(id, FAIL + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
                    }

                }
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
                return true;
            }

        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            outcome.messagesByInstanceId.put(id, FAIL + "Generic Exception: " + msg);
            contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
            Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
            return true;
        }

        // If response from server is valid use that else use default messaging
        if (messageParser.isValid()) {
            outcome.messagesByInstanceId.put(id, messageParser.getMessageResponse());
        } else {
            // Default messaging
            outcome.messagesByInstanceId.put(id, Collect.getInstance().getString(R.string.success));
        }

        contentValues.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);

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
