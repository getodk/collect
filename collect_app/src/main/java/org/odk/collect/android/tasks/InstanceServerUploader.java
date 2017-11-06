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
import android.webkit.MimeTypeMap;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ResponseMessageParser;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.Header;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.NoHttpResponseException;
import org.opendatakit.httpclientandroidlib.client.ClientProtocolException;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpHead;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.conn.ConnectTimeoutException;
import org.opendatakit.httpclientandroidlib.conn.HttpHostConnectException;
import org.opendatakit.httpclientandroidlib.entity.ContentType;
import org.opendatakit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import org.opendatakit.httpclientandroidlib.entity.mime.content.FileBody;
import org.opendatakit.httpclientandroidlib.entity.mime.content.StringBody;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploader extends InstanceUploader {

    private static enum ContentTypeMapping {
        XML("xml",  ContentType.TEXT_XML),
      _3GPP("3gpp", ContentType.create("audio/3gpp")),
       _3GP("3gp",  ContentType.create("video/3gpp")),
        AVI("avi",  ContentType.create("video/avi")),
        AMR("amr",  ContentType.create("audio/amr")),
        CSV("csv",  ContentType.create("text/csv")),
        JPG("jpg",  ContentType.create("image/jpeg")),
        MP3("mp3",  ContentType.create("audio/mp3")),
        MP4("mp4",  ContentType.create("video/mp4")),
        OGA("oga",  ContentType.create("audio/ogg")),
        OGG("ogg",  ContentType.create("audio/ogg")),
        OGV("ogv",  ContentType.create("video/ogg")),
        WAV("wav",  ContentType.create("audio/wav")),
       WEBM("webm", ContentType.create("video/webm")),
        XLS("xls",  ContentType.create("application/vnd.ms-excel"));

        private String extension;
        private ContentType contentType;

        ContentTypeMapping(String extension, ContentType contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public static ContentType of(String fileName) {
            String extension = getFileExtension(fileName);

            for (ContentTypeMapping m : values()) {
                if (m.extension.equals(extension)) {
                    return m.contentType;
                }
            }

            return null;
        }
    }

    // it can take up to 27 seconds to spin up Aggregate
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final String fail = "Error: ";
    private static final String URL_PATH_SEP = "/";

    /**
     * Uploads to urlString the submission identified by id with filepath of instance
     *
     * @param urlString    destination URL
     * @param toUpdate     - Instance URL for recording status update.
     * @param localContext - context (e.g., credentials, cookies) for client connection
     * @param uriRemap     - mapping of Uris to avoid redirects on subsequent invocations
     * @return false if credentials are required and we should terminate immediately.
     */
    private boolean uploadOneSubmission(String urlString, String id, String instanceFilePath,
                                        Uri toUpdate, HttpContext localContext, Map<Uri, Uri> uriRemap, Outcome outcome) {

        Collect.getInstance().getActivityLogger().logAction(this, urlString, instanceFilePath);

        File instanceFile = new File(instanceFilePath);
        ContentValues cv = new ContentValues();
        Uri u = Uri.parse(urlString);
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        ResponseMessageParser messageParser = null;
        boolean openRosaServer = false;
        if (uriRemap.containsKey(u)) {
            // we already issued a head request and got a response,
            // so we know the proper URL to send the submission to
            // and the proper scheme. We also know that it was an
            // OpenRosa compliant server.
            openRosaServer = true;
            u = uriRemap.get(u);

            // if https then enable preemptive basic auth...
            if (u.getScheme().equals("https")) {
                WebUtils.enablePreemptiveBasicAuth(localContext, u.getHost());
            }

            Timber.i("Using Uri remap for submission %s. Now: %s", id, u.toString());
        } else {
            if (u.getHost() == null) {
                Timber.i("Host name may not be null");
                outcome.results.put(id, fail + "Host name may not be null");
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                return true;
            }

            // if https then enable preemptive basic auth...
            if (u.getScheme() != null && u.getScheme().equals("https")) {
                WebUtils.enablePreemptiveBasicAuth(localContext, u.getHost());
            }

            // we need to issue a head request
            HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);

            // prepare response
            HttpResponse response = null;
            try {
                Timber.i("Issuing HEAD request for %s to: %s", id, u.toString());

                response = httpclient.execute(httpHead, localContext);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    Collect.getInstance().getCookieStore().clear();

                    WebUtils.discardEntityBytes(response);
                    // we need authentication, so stop and return what we've
                    // done so far.
                    outcome.authRequestingServer = u;
                    return false;
                } else if (statusCode == 204) {
                    Header[] locations = response.getHeaders("Location");
                    WebUtils.discardEntityBytes(response);
                    if (locations != null && locations.length == 1) {
                        try {
                            Uri newURI = Uri.parse(
                                    URLDecoder.decode(locations[0].getValue(), "utf-8"));
                            if (u.getHost().equalsIgnoreCase(newURI.getHost())) {
                                openRosaServer = true;
                                // trust the server to tell us a new location
                                // ... and possibly to use https instead.
                                uriRemap.put(u, newURI);
                                u = newURI;
                            } else {
                                // Don't follow a redirection attempt to a different host.
                                // We can't tell if this is a spoof or not.
                                outcome.results.put(
                                        id,
                                        fail
                                                + "Unexpected redirection attempt to a different "
                                                + "host: "
                                                + newURI.toString());
                                cv.put(InstanceColumns.STATUS,
                                        InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                                Collect.getInstance().getContentResolver()
                                        .update(toUpdate, cv, null, null);
                                return true;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Exception thrown parsing URI for url %s", urlString);
                            outcome.results.put(id, fail + urlString + " " + e.toString());
                            cv.put(InstanceColumns.STATUS,
                                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                            Collect.getInstance().getContentResolver()
                                    .update(toUpdate, cv, null, null);
                            return true;
                        }
                    }
                } else {
                    // may be a server that does not handle
                    WebUtils.discardEntityBytes(response);

                    Timber.w("Status code on Head request: %d", statusCode);
                    if (statusCode >= HttpStatus.SC_OK
                            && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
                        outcome.results.put(
                                id,
                                fail
                                        + "Invalid status code on Head request.  If you have a "
                                        + "web proxy, you may need to login to your network. ");
                        cv.put(InstanceColumns.STATUS,
                                InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                        Collect.getInstance().getContentResolver()
                                .update(toUpdate, cv, null, null);
                        return true;
                    }
                }
            } catch (ClientProtocolException | ConnectTimeoutException | UnknownHostException | SocketTimeoutException | NoHttpResponseException | SocketException e) {
                if (e instanceof ClientProtocolException) {
                    outcome.results.put(id, fail + "Client Protocol Exception");
                    Timber.i(e, "Client Protocol Exception");
                } else if (e instanceof ConnectTimeoutException) {
                    outcome.results.put(id, fail + "Connection Timeout");
                    Timber.i(e, "Connection Timeout");
                } else if (e instanceof UnknownHostException) {
                    outcome.results.put(id, fail + e.toString() + " :: Network Connection Failed");
                    Timber.i(e, "Network Connection Failed");
                } else if (e instanceof SocketTimeoutException) {
                    outcome.results.put(id, fail + "Connection Timeout");
                    Timber.i(e, "Connection timeout");
                } else {
                    outcome.results.put(id, fail + "Network Connection Refused");
                    Timber.i(e, "Network Connection Refused");
                }
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                return true;
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }
                outcome.results.put(id, fail + "Generic Exception: " + msg);
                Timber.e(e);
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
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
        File submissionFile = new File(instanceFile.getParentFile(), "submission.xml");
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            outcome.results.put(id, fail + "instance XML file does not exist!");
            cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
            Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
            return true;
        }

        // find all files in parent directory
        File[] allFiles = instanceFile.getParentFile().listFiles();

        // add media files
        List<File> files = new ArrayList<File>();
        if (allFiles != null) {
            for (File f : allFiles) {
                String fileName = f.getName();

                if (fileName.startsWith(".")) {
                    continue; // ignore invisible files
                } else if (fileName.equals(instanceFile.getName())) {
                    continue; // the xml file has already been added
                } else if (fileName.equals(submissionFile.getName())) {
                    continue; // the xml file has already been added
                }

                String extension = getFileExtension(fileName);

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
        } else {
            return false;
        }

        boolean first = true;
        int j = 0;
        int lastJ;
        while (j < files.size() || first) {
            lastJ = j;
            first = false;

            MimeTypeMap m = MimeTypeMap.getSingleton();

            long byteCount = 0L;

            // mime post
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // add the submission file first...
            FileBody fb = new FileBody(submissionFile, ContentType.TEXT_XML);
            builder.addPart("xml_submission_file", fb);
            Timber.i("added xml_submission_file: %s", submissionFile.getName());
            byteCount += submissionFile.length();

            for (; j < files.size(); j++) {
                File f = files.get(j);

                // we will be processing every one of these, so
                // we only need to deal with the content type determination...
                ContentType contentType = ContentTypeMapping.of(f.getName());
                if (contentType == null) {
                    String mime = m.getMimeTypeFromExtension(getFileExtension(f.getName()));
                    if (mime != null) {
                        contentType = ContentType.create(mime);
                    } else {
                        Timber.w("No specific MIME type found for file: %s", f.getName());
                        contentType = ContentType.APPLICATION_OCTET_STREAM;
                    }
                }
                fb = new FileBody(f, contentType);
                builder.addPart(f.getName(), fb);
                byteCount += f.length();
                Timber.i("added file of type '%s' %s", contentType, f.getName());

                // we've added at least one attachment to the request...
                if (j + 1 < files.size()) {
                    if ((j - lastJ + 1 > 100) || (byteCount + files.get(j + 1).length()
                            > 10000000L)) {
                        // the next file would exceed the 10MB threshold...
                        Timber.i("Extremely long post is being split into multiple posts");
                        try {
                            StringBody sb = new StringBody("yes",
                                    ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8")));
                            builder.addPart("*isIncomplete*", sb);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                        ++j; // advance over the last attachment added...
                        break;
                    }
                }
            }

            HttpPost httppost = WebUtils.createOpenRosaHttpPost(u);
            httppost.setEntity(builder.build());

            // prepare response and return uploaded
            HttpResponse response;

            try {
                Timber.i("Issuing POST request for %s to: %s", id, u.toString());
                response = httpclient.execute(httppost, localContext);
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity httpEntity = response.getEntity();
                messageParser = new ResponseMessageParser(httpEntity);
                WebUtils.discardEntityBytes(response);
                Timber.i("Response code:%d", responseCode);
                // verify that the response was a 201 or 202.
                // If it wasn't, the submission has failed.
                if (responseCode != HttpStatus.SC_CREATED
                        && responseCode != HttpStatus.SC_ACCEPTED) {
                    if (responseCode == HttpStatus.SC_OK) {
                        outcome.results.put(id, fail + "Network login failure? Again?");
                    } else if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                        // clear the cookies -- should not be necessary?
                        Collect.getInstance().getCookieStore().clear();
                        outcome.results.put(id, fail + response.getStatusLine().getReasonPhrase()
                                + " (" + responseCode + ") at " + urlString);
                    } else {
                        // If response from server is valid use that else use default messaging
                        if (messageParser.isValid()) {
                            outcome.results.put(id, fail + messageParser.getMessageResponse());
                        } else {
                            outcome.results.put(id, fail + response.getStatusLine().getReasonPhrase()
                                    + " (" + responseCode + ") at " + urlString);
                        }

                    }
                    cv.put(InstanceColumns.STATUS,
                            InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                    Collect.getInstance().getContentResolver()
                            .update(toUpdate, cv, null, null);
                    return true;
                }
            } catch (IOException e) {
                if (e instanceof UnknownHostException || e instanceof HttpHostConnectException
                        || e instanceof SocketException || e instanceof NoHttpResponseException
                        || e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
                    Timber.i(e);
                } else {
                    Timber.e(e);
                }
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }
                outcome.results.put(id, fail + "Generic Exception: " + msg);
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                return true;
            }
        }

        // If response from server is valid use that else use default messaging
        if (messageParser.isValid()) {
            outcome.results.put(id, messageParser.getMessageResponse());
        } else {
            // Default messaging
            outcome.results.put(id, Collect.getInstance().getString(R.string.success));
        }

        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
        return true;
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
                selectionBuf.append(",");
            }
            selectionBuf.append("?");
            selectionArgs[i] = values[i + low].toString();
        }

        selectionBuf.append(")");
        String selection = selectionBuf.toString();

        String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                .getSingularProperty(PropertyManager.withUri(PropertyManager.PROPMGR_DEVICE_ID));

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();

        Map<Uri, Uri> uriRemap = new HashMap<Uri, Uri>();

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

                    if (!uploadOneSubmission(urlString, id, instance, toUpdate, localContext,
                            uriRemap, outcome)) {
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

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
