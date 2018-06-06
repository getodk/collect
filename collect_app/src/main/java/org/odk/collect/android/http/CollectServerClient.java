/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.http;

import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ResponseMessageParser;
import org.xmlpull.v1.XmlPullParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import timber.log.Timber;

/**
 * Common utility methods for managing the credentials associated with the
 * request context and constructing http context, client and request with the
 * proper parameters and OpenRosa headers.
 *
 * @author mitchellsundt@gmail.com
 */
public final class CollectServerClient {

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    private static final String fail = "Error: ";

    private static HttpInterface httpConnection;

    private CollectServerClient() {}

    /**
     * Gets an object that conforms to the HttpInterface. This is a protected method
     * so that it can be overridden in a Mock CollectServerClient class to allow CollectServerClient to be unit tested.
     *
     * @return instance of HttpInterface
     */
    protected static synchronized HttpInterface getHttpConnection() {
        if (httpConnection == null) {
            httpConnection = new HttpClientConnection();
        }
        return httpConnection;
    }

    /**
     * Remove all credentials for accessing the specified host.
     *
     * @param host host to clear the credentials
     */
    public static void clearHostCredentials(String host) {
        getHttpConnection().clearHostCredentials(host);
    }

    /**
     * Clears the cookie store
     */
    public static void clearCookieStore() {
        getHttpConnection().clearCookieStore();
    }

    /**
     * Add credentials to a specified host
     *
     * @param username - The users name
     * @param password - The password
     * @param host - The host to add credentials to
     */
    public static void addCredentials(String username, String password,
                                      String host) {
        getHttpConnection().addCredentials(username, password, host);
    }

    /**
     * Common method for returning a parsed xml document given a url and the
     * http context and client objects involved in the web connection.
     */
    public static DocumentFetchResult getXmlDocument(String urlString) {

        // parse response
        Document doc;

        HttpInputStreamResult inputStreamResult;
        try {
            inputStreamResult = getHttpInputStream(urlString, HTTP_CONTENT_TYPE_TEXT_XML);

            InputStream is = inputStreamResult.getInputStream();
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is, "UTF-8");
                doc = new Document();
                KXmlParser parser = new KXmlParser();
                parser.setInput(isr);
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                doc.parse(parser);
                isr.close();
                isr = null;
            } finally {
                if (isr != null) {
                    try {
                        // ensure stream is consumed...
                        final long count = 1024L;
                        while (isr.skip(count) == count) {
                            // skipping to the end of the http entity
                        }

                        isr.close();

                    } catch (IOException e) {
                        // no-op
                        Timber.e(e, "Error closing input stream reader");
                    }
                }

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Timber.e(e, "Error closing inputstream");
                        // no-op
                    }
                }
            }
        } catch (Exception e) {
            String error = "Parsing failed with " + e.getMessage()
                    + " while accessing " + urlString;
            Timber.e(error);
            return new DocumentFetchResult(error, 0);
        }

        return new DocumentFetchResult(doc, inputStreamResult.isOpenRosaResponse(), inputStreamResult.getHash());
    }


    /**
     * Creates a http connection and sets up an input stream.
     *
     * @param downloadUrl uri of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @return HttpInputStreamResult - An object containing the Stream, Hash and Headers
     * @throws Exception - Can throw a multitude of Exceptions, such as MalformedURLException or IOException
     */
    public static @NonNull
    HttpInputStreamResult getHttpInputStream(@NonNull String downloadUrl, final String contentType) throws Exception {
        URI uri;
        try {
            // assume the downloadUrl is escaped properly
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            Timber.e(e, "Unable to get a URI for download URL : %s  due to %s : ", downloadUrl, e.getMessage());
            throw e;
        }

        if (uri.getHost() == null) {
            throw new Exception("Invalid server URL (no hostname): " + downloadUrl);
        }

        HttpInterface httpConnection = getHttpConnection();
        return httpConnection.getHttpInputStream(uri, contentType);
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
    public static boolean uploadSubmissionFile(String urlString, String id, String instanceFilePath,
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
                outcome.messagesByInstanceId.put(id, fail + "Host name may not be null");
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
                HttpInterface connection = getHttpConnection();
                Map<String, String> responseHeaders = new HashMap<>();
                int statusCode = connection.httpHeadRequest(uri, responseHeaders);

                if (statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    outcome.authRequestingServer = submissionUri;
                    return false;
                } else if (statusCode == HttpsURLConnection.HTTP_NO_CONTENT) {
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
                                        fail
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
                            outcome.messagesByInstanceId.put(id, fail + urlString + " " + e.toString());
                            contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS,
                                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                            Collect.getInstance().getContentResolver()
                                    .update(toUpdate, contentValues, null, null);
                            return true;
                        }
                    }

                } else {
                    Timber.w("Status code on Head request: %d", statusCode);
                    if (statusCode >= HttpsURLConnection.HTTP_OK && statusCode < HttpsURLConnection.HTTP_MULT_CHOICE) {
                        outcome.messagesByInstanceId.put(
                                id,
                                fail
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

                outcome.messagesByInstanceId.put(id, fail + msg);
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
            outcome.messagesByInstanceId.put(id, fail + "instance XML file does not exist!");
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
            messageParser = getHttpConnection().uploadSubmissionFile(files, submissionFile, URI.create(submissionUri.toString()));
            int responseCode = messageParser.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_CREATED && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    outcome.messagesByInstanceId.put(id, fail + "Network login failure? Again?");
                } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    outcome.messagesByInstanceId.put(id, fail + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
                } else {
                    // If response from server is valid use that else use default messaging
                    if (messageParser.isValid()) {
                        outcome.messagesByInstanceId.put(id, fail + messageParser.getMessageResponse());
                    } else {
                        outcome.messagesByInstanceId.put(id, fail + messageParser.getReasonPhrase() + " (" + responseCode + ") at " + urlString);
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
            outcome.messagesByInstanceId.put(id, fail + "Generic Exception: " + msg);
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

        contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
        return true;
    }

    private static List<File> getFilesInParentDirectory(File instanceFile, File submissionFile, boolean openRosaServer) {
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


    public static String getPlainTextMimeType() {
        return "text/plain";
    }

    public static class Outcome {
        public Uri authRequestingServer = null;
        public boolean invalidOAuth;
        public HashMap<String, String> messagesByInstanceId = new HashMap<>();
    }
}