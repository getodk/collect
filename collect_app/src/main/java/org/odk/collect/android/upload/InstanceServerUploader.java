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

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.upload.result.SubmissionUploadAuthRequested;
import org.odk.collect.android.upload.result.SubmissionUploadFatalError;
import org.odk.collect.android.upload.result.SubmissionUploadNonFatalError;
import org.odk.collect.android.upload.result.SubmissionUploadResult;
import org.odk.collect.android.upload.result.SubmissionUploadSuccess;
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
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class InstanceServerUploader extends InstanceUploader {
    private static final String URL_PATH_SEP = "/";
    private static final String FAIL = "Error: ";

    private final OpenRosaHttpInterface httpInterface;
    private final WebCredentialsUtils webCredentialsUtils;

    public InstanceServerUploader(OpenRosaHttpInterface httpInterface,
                                  WebCredentialsUtils webCredentialsUtils) {
        this.httpInterface = httpInterface;
        this.webCredentialsUtils = webCredentialsUtils;
    }

    /**
     * Uploads all files associated with an instance to the specified URL. Writes fail/success
     * status to database. Logs an analytics event in case of success.
     */
    public SubmissionUploadResult uploadOneSubmission(Instance instance, String urlString,
                                                       Map<Uri, Uri> uriRemap) {
        Uri submissionUri = Uri.parse(urlString);

        // Used to determine if attachments should be sent for Aggregate < 0.9x servers
        boolean openRosaServer = false;

        // We already issued a head request and got a response, so we know it was an
        // OpenRosa-compliant server. We also know the proper URL to send the submission to and
        // the proper scheme.
        if (uriRemap.containsKey(submissionUri)) {
            openRosaServer = true;
            submissionUri = uriRemap.get(submissionUri);
            Timber.i("Using Uri remap for submission %s. Now: %s", instance.getDatabaseId(),
                    submissionUri.toString());
        } else {
            if (submissionUri.getHost() == null) {
                saveFailedStatusToDatabase(instance);
                return new SubmissionUploadNonFatalError(FAIL + "Host name may not be null");
            }

            URI uri;
            try {
                uri = URI.create(submissionUri.toString());
            } catch (IllegalArgumentException e) {
                saveFailedStatusToDatabase(instance);
                // TODO: should this really be fatal? Seems the next submission might not have this
                // problem
                return new SubmissionUploadFatalError(R.string.url_error,
                        e.getMessage() != null ? e.getMessage() : e.toString());
            }

            try {
                HttpHeadResult headResult = httpInterface.head(uri, webCredentialsUtils.getCredentials(uri));
                Map<String, String> responseHeaders = headResult.getHeaders();

                if (headResult.getStatusCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    return new SubmissionUploadAuthRequested(submissionUri);
                } else if (headResult.getStatusCode() == HttpsURLConnection.HTTP_NO_CONTENT) {
                    // Redirect header received
                    if (responseHeaders.containsKey("Location")) {
                        try {
                            Uri newURI = Uri.parse(URLDecoder.decode(responseHeaders.get("Location"), "utf-8"));
                            // Allow redirects within same host. This could be redirecting to HTTPS.
                            if (submissionUri.getHost().equalsIgnoreCase(newURI.getHost())) {
                                openRosaServer = true;
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
                                saveFailedStatusToDatabase(instance);
                                return new SubmissionUploadNonFatalError(FAIL
                                        + "Unexpected redirection attempt to a different "
                                        + "host: "
                                        + newURI.toString());
                            }
                        } catch (Exception e) {
                            saveFailedStatusToDatabase(instance);
                            return new SubmissionUploadNonFatalError(FAIL + urlString + " " + e.toString());
                        }
                    }

                } else {
                    Timber.w("Status code on Head request: %d", headResult.getStatusCode());
                    if (headResult.getStatusCode() >= HttpsURLConnection.HTTP_OK
                            && headResult.getStatusCode() < HttpsURLConnection.HTTP_MULT_CHOICE) {
                        saveFailedStatusToDatabase(instance);
                        return new SubmissionUploadNonFatalError(FAIL
                                + "Invalid status code on Head request. If you have a "
                                + "web proxy, you may need to login to your network. ");
                    }
                }
            } catch (Exception e) {
                saveFailedStatusToDatabase(instance);
                return new SubmissionUploadNonFatalError(FAIL
                        + (e.getMessage() != null ? e.getMessage() : e.toString()));
            }
        }

        // When encrypting submissions, there is a failure window that may mark the submission as
        // complete but leave the file-to-be-uploaded with the name "submission.xml" and the plaintext
        // submission files on disk.  In this case, upload the submission.xml and all the files in
        // the directory. This means the plaintext files and the encrypted files will be sent to the
        // server and the server will have to figure out what to do with them.
        File instanceFile = new File(instance.getInstanceFilePath());
        File submissionFile = new File(instanceFile.getParentFile(), "submission.xml");
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            saveFailedStatusToDatabase(instance);
            return new SubmissionUploadNonFatalError(FAIL + "instance XML file does not exist!");
        }

        List<File> files = getFilesInParentDirectory(instanceFile, submissionFile, openRosaServer);

        // TODO: when can this happen? Why does it cause the whole submission attempt to fail?
        if (files == null) {
            return new SubmissionUploadFatalError("Error reading files to upload");
        }

        ResponseMessageParser messageParser;

        try {
            URI uri = URI.create(submissionUri.toString());

            messageParser = httpInterface.uploadSubmissionFile(files, submissionFile, uri,
                    webCredentialsUtils.getCredentials(uri));

            int responseCode = messageParser.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_CREATED && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                SubmissionUploadResult result;
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    result = new SubmissionUploadNonFatalError(FAIL + "Network login failure? Again?");
                } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    result = new SubmissionUploadNonFatalError(FAIL + messageParser.getReasonPhrase()
                            + " (" + responseCode + ") at " + urlString);
                } else {
                    if (messageParser.isValid()) {
                        result = new SubmissionUploadNonFatalError(FAIL + messageParser.getMessageResponse());
                    } else {
                        result = new SubmissionUploadNonFatalError(FAIL + messageParser.getReasonPhrase()
                                + " (" + responseCode + ") at " + urlString);
                    }

                }
                saveFailedStatusToDatabase(instance);
                return result;
            }

        } catch (IOException e) {
            saveFailedStatusToDatabase(instance);
            return new SubmissionUploadNonFatalError(FAIL + "Generic Exception: "
                    + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }

        saveSuccessStatusToDatabase(instance);

        if (messageParser.isValid()) {
            return new SubmissionUploadSuccess(messageParser.getMessageResponse());
        } else {
            return new SubmissionUploadSuccess();
        }
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


    /**
     * Returns the URL this instance should be submitted to with appended deviceId.
     *
     * If the upload was triggered by an external app and specified an override URL, use that one.
     * Otherwise, use the submission URL configured in the form
     * (https://opendatakit.github.io/xforms-spec/#submission-attributes). Finally, default to the
     * URL configured at the app level.
     */
    @NonNull
    public String getUrlToSubmitTo(Instance currentInstance, String deviceId, String overrideURL) {
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

    @NonNull String getUrlToSubmitTo(Instance currentInstance, String deviceId) {
        return getUrlToSubmitTo(currentInstance, deviceId, null);
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
