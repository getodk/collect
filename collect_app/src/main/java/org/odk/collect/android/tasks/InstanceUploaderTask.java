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
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.InstanceUploaderListener;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderTask extends AsyncTask<Long, Integer, InstanceUploaderTask.Outcome> {

    private static final String t = "InstanceUploaderTask";
    // it can take up to 27 seconds to spin up Aggregate
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final String fail = "Error: ";
    private static final String URL_PATH_SEP = "/";

    private InstanceUploaderListener mStateListener;

    public static class Outcome {
        public Uri mAuthRequestingServer = null;
        public HashMap<String, String> mResults = new HashMap<String, String>();
    }

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

            Log.i(t, "Using Uri remap for submission " + id + ". Now: " + u.toString());
        } else {

            // if https then enable preemptive basic auth...
            if (u.getScheme() != null && u.getScheme().equals("https")) {
                WebUtils.enablePreemptiveBasicAuth(localContext, u.getHost());
            }

            // we need to issue a head request
            HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);

            // prepare response
            HttpResponse response = null;
            try {
                Log.i(t, "Issuing HEAD request for " + id + " to: " + u.toString());

                response = httpclient.execute(httpHead, localContext);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    Collect.getInstance().getCookieStore().clear();

                    WebUtils.discardEntityBytes(response);
                    // we need authentication, so stop and return what we've
                    // done so far.
                    outcome.mAuthRequestingServer = u;
                    return false;
                } else if (statusCode == 204) {
                    Header[] locations = response.getHeaders("Location");
                    WebUtils.discardEntityBytes(response);
                    if (locations != null && locations.length == 1) {
                        try {
                            Uri uNew = Uri.parse(
                                    URLDecoder.decode(locations[0].getValue(), "utf-8"));
                            if (u.getHost().equalsIgnoreCase(uNew.getHost())) {
                                openRosaServer = true;
                                // trust the server to tell us a new location
                                // ... and possibly to use https instead.
                                uriRemap.put(u, uNew);
                                u = uNew;
                            } else {
                                // Don't follow a redirection attempt to a different host.
                                // We can't tell if this is a spoof or not.
                                outcome.mResults.put(
                                        id,
                                        fail
                                                + "Unexpected redirection attempt to a different "
                                                + "host: "
                                                + uNew.toString());
                                cv.put(InstanceColumns.STATUS,
                                        InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                                Collect.getInstance().getContentResolver()
                                        .update(toUpdate, cv, null, null);
                                return true;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Exception thrown parsing URI for url %s due to : %s ", urlString, e.getMessage());
                            outcome.mResults.put(id, fail + urlString + " " + e.toString());
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

                    Log.w(t, "Status code on Head request: " + statusCode);
                    if (statusCode >= HttpStatus.SC_OK
                            && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
                        outcome.mResults.put(
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
            } catch (ClientProtocolException | ConnectTimeoutException | UnknownHostException | SocketTimeoutException | HttpHostConnectException e) {
                if (e instanceof  ClientProtocolException) {
                    outcome.mResults.put(id, fail + "Client Protocol Exception");
                    Timber.e(e, "Client Protocol Exception thrown due to : %s ", e.getMessage());
                } else if (e instanceof  ConnectTimeoutException) {
                    outcome.mResults.put(id, fail + "Connection Timeout");
                    Timber.e(e, "Connection Timeout due to : %s ", e.getMessage());
                } else if (e instanceof  UnknownHostException) {
                    outcome.mResults.put(id, fail + e.toString() + " :: Network Connection Failed");
                    Timber.e(e, "Network Connection Failed due to : %s ", e.getMessage());
                } else if (e instanceof  SocketTimeoutException) {
                    outcome.mResults.put(id, fail + "Connection Timeout");
                    Timber.e(e, "Connection timeout due to : %s ", e.getMessage());
                } else {
                    outcome.mResults.put(id, fail + "Network Connection Refused");
                    Timber.e(e,"Network Connection Refused due to : %s ", e.getMessage());
                }
                Log.e(t, e.toString());
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                return true;
            } catch (Exception e) {
                Log.e(t, e.toString());
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }
                outcome.mResults.put(id, fail + "Generic Exception: " + msg);
                Timber.e(e, e.getMessage());
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
            Log.w(t,
                    "submission.xml will be uploaded instead of " + instanceFile.getAbsolutePath());
        } else {
            submissionFile = instanceFile;
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            outcome.mResults.put(id, fail + "instance XML file does not exist!");
            cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
            Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
            return true;
        }

        // find all files in parent directory
        File[] allFiles = instanceFile.getParentFile().listFiles();

        // add media files
        List<File> files = new ArrayList<File>();
        for (File f : allFiles) {
            String fileName = f.getName();

            int dotIndex = fileName.lastIndexOf(".");
            String extension = "";
            if (dotIndex != -1) {
                extension = fileName.substring(dotIndex + 1);
            }

            if (fileName.startsWith(".")) {
                // ignore invisible files
                continue;
            }
            if (fileName.equals(instanceFile.getName())) {
                continue; // the xml file has already been added
            } else if (fileName.equals(submissionFile.getName())) {
                continue; // the xml file has already been added
            } else if (openRosaServer) {
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
                Log.w(t, "unrecognized file type " + f.getName());
            }
        }

        boolean first = true;
        int j = 0;
        int lastJ;
        while (j < files.size() || first) {
            lastJ = j;
            first = false;

            HttpPost httppost = WebUtils.createOpenRosaHttpPost(u);

            MimeTypeMap m = MimeTypeMap.getSingleton();

            long byteCount = 0L;

            // mime post
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // add the submission file first...
            FileBody fb = new FileBody(submissionFile, ContentType.TEXT_XML);
            builder.addPart("xml_submission_file", fb);
            Log.i(t, "added xml_submission_file: " + submissionFile.getName());
            byteCount += submissionFile.length();

            for (; j < files.size(); j++) {
                File f = files.get(j);
                String fileName = f.getName();
                int idx = fileName.lastIndexOf(".");
                String extension = "";
                if (idx != -1) {
                    extension = fileName.substring(idx + 1);
                }
                String contentType = m.getMimeTypeFromExtension(extension);

                // we will be processing every one of these, so
                // we only need to deal with the content type determination...
                if (extension.equals("xml")) {
                    fb = new FileBody(f, ContentType.TEXT_XML);
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added xml file " + f.getName());
                } else if (extension.equals("3gpp")) {
                    fb = new FileBody(f, ContentType.create("audio/3gpp"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added audio file " + f.getName());
                } else if (extension.equals("3gp")) {
                    fb = new FileBody(f, ContentType.create("video/3gpp"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("avi")) {
                    fb = new FileBody(f, ContentType.create("video/avi"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("amr")) {
                    fb = new FileBody(f, ContentType.create("audio/amr"));
                    builder.addPart(f.getName(), fb);
                    Log.i(t, "added audio file " + f.getName());
                } else if (extension.equals("csv")) {
                    fb = new FileBody(f, ContentType.create("text/csv"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added csv file " + f.getName());
                } else if (extension.equals("jpg")) {
                    fb = new FileBody(f, ContentType.create("image/jpeg"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added image file " + f.getName());
                } else if (extension.equals("mp3")) {
                    fb = new FileBody(f, ContentType.create("audio/mp3"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added audio file " + f.getName());
                } else if (extension.equals("mp4")) {
                    fb = new FileBody(f, ContentType.create("video/mp4"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("oga")) {
                    fb = new FileBody(f, ContentType.create("audio/ogg"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added audio file " + f.getName());
                } else if (extension.equals("ogg")) {
                    fb = new FileBody(f, ContentType.create("audio/ogg"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("ogv")) {
                    fb = new FileBody(f, ContentType.create("video/ogg"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("wav")) {
                    fb = new FileBody(f, ContentType.create("audio/wav"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added audio file " + f.getName());
                } else if (extension.equals("webm")) {
                    fb = new FileBody(f, ContentType.create("video/webm"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added video file " + f.getName());
                } else if (extension.equals("xls")) {
                    fb = new FileBody(f, ContentType.create("application/vnd.ms-excel"));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t, "added xls file " + f.getName());
                } else if (contentType != null) {
                    fb = new FileBody(f, ContentType.create(contentType));
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.i(t,
                            "added recognized filetype (" + contentType + ") " + f.getName());
                } else {
                    contentType = "application/octet-stream";
                    fb = new FileBody(f, ContentType.APPLICATION_OCTET_STREAM);
                    builder.addPart(f.getName(), fb);
                    byteCount += f.length();
                    Log.w(t, "added unrecognized file (" + contentType + ") " + f.getName());
                }

                // we've added at least one attachment to the request...
                if (j + 1 < files.size()) {
                    if ((j - lastJ + 1 > 100) || (byteCount + files.get(j + 1).length()
                            > 10000000L)) {
                        // the next file would exceed the 10MB threshold...
                        Log.i(t, "Extremely long post is being split into multiple posts");
                        try {
                            StringBody sb = new StringBody("yes",
                                    ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8")));
                            builder.addPart("*isIncomplete*", sb);
                        } catch (Exception e) {
                            Timber.e(e, e.getMessage());
                        }
                        ++j; // advance over the last attachment added...
                        break;
                    }
                }
            }

            httppost.setEntity(builder.build());

            // prepare response and return uploaded
            HttpResponse response = null;

            try {
                Log.i(t, "Issuing POST request for " + id + " to: " + u.toString());
                response = httpclient.execute(httppost, localContext);
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity httpEntity = response.getEntity();
                messageParser = new ResponseMessageParser(httpEntity);
                WebUtils.discardEntityBytes(response);
                Log.i(t, "Response code:" + responseCode);
                // verify that the response was a 201 or 202.
                // If it wasn't, the submission has failed.
                if (responseCode != HttpStatus.SC_CREATED
                        && responseCode != HttpStatus.SC_ACCEPTED) {
                    if (responseCode == HttpStatus.SC_OK) {
                        outcome.mResults.put(id, fail + "Network login failure? Again?");
                    } else if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                        // clear the cookies -- should not be necessary?
                        Collect.getInstance().getCookieStore().clear();
                        outcome.mResults.put(id, fail + response.getStatusLine().getReasonPhrase()
                                + " (" + responseCode + ") at " + urlString);
                    } else {
                        // If response from server is valid use that else use default messaging
                        if (messageParser.isValid()){
                            outcome.mResults.put(id, fail + messageParser.getMessageResponse());
                        }else{
                            outcome.mResults.put(id, fail + response.getStatusLine().getReasonPhrase()
                                    + " (" + responseCode + ") at " + urlString);
                        }

                    }
                    cv.put(InstanceColumns.STATUS,
                            InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                    Collect.getInstance().getContentResolver()
                            .update(toUpdate, cv, null, null);
                    return true;
                }
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
                Log.e(t, e.toString());
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }
                outcome.mResults.put(id, fail + "Generic Exception: " + msg);
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                return true;
            }
        }

        // If response from server is valid use that else use default messaging
        if (messageParser.isValid()){
            outcome.mResults.put(id, messageParser.getMessageResponse());
        }else{
            // Default messaging
            outcome.mResults.put(id, Collect.getInstance().getString(R.string.success));
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
                .getSingularProperty(PropertyManager.OR_DEVICE_ID_PROPERTY);

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
                    String urlString = c.isNull(subIdx) ?
                            getServerSubmissionURL() : c.getString(subIdx).trim();

                    // add the deviceID to the request...
                    try {
                        urlString += "?deviceID=" + URLEncoder.encode(deviceId, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // unreachable...
                        Timber.i(e, "Error encoding URL for devise id : %s due to : %s ", deviceId, e.getMessage());
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
            counter ++;
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

    @Override
    protected void onPostExecute(Outcome outcome) {
        synchronized (this) {
            if (mStateListener != null) {
                if (outcome.mAuthRequestingServer != null) {
                    mStateListener.authRequest(outcome.mAuthRequestingServer, outcome.mResults);
                } else {
                    mStateListener.uploadingComplete(outcome.mResults);

                    Set<String> keys = outcome.mResults.keySet();
                    Iterator<String> it = keys.iterator();
                    int count = keys.size();
                    while(count > 0){
                        String[] selectionArgs = null;
                        if(count > ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER - 1) {
                            selectionArgs = new String[
                                    ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER];
                        } else {
                            selectionArgs = new String[count + 1];
                        }

                        StringBuilder selection = new StringBuilder();

                        selection.append(InstanceColumns._ID + " IN (");
                        int i = 0;

                        while (it.hasNext() && i < selectionArgs.length - 1){
                            selectionArgs[i] = it.next();
                            selection.append("?");

                            if (i != selectionArgs.length - 2) {
                                selection.append(",");
                            }
                            i++;
                        }

                        count -= selectionArgs.length - 1;
                        selection.append(")");
                        selection.append(" and status=?");
                        selectionArgs[i] = InstanceProviderAPI.STATUS_COMPLETE;

                        Cursor results = null;
                        try {
                            results =
                                    new InstancesDao().getInstancesCursor(selection.toString()
                                            , selectionArgs);
                            if (results.getCount() > 0) {
                                Long[] toDelete = new Long[results.getCount()];
                                results.moveToPosition(-1);

                                int cnt = 0;
                                while (results.moveToNext()) {
                                    toDelete[cnt] = results.getLong(results
                                            .getColumnIndex(InstanceColumns._ID));
                                    cnt++;
                                }

                                boolean deleteFlag = PreferenceManager.getDefaultSharedPreferences(
                                        Collect.getInstance().getApplicationContext()).getBoolean(
                                        PreferenceKeys.KEY_DELETE_AFTER_SEND, false);
                                if (deleteFlag) {
                                    DeleteInstancesTask dit = new DeleteInstancesTask();
                                    dit.setContentResolver(
                                            Collect.getInstance().getContentResolver());
                                    dit.execute(toDelete);
                                }

                            }
                        } catch (SQLException e){
                            Log.e(t, e.getMessage(), e);
                        } finally{
                            if (results != null) {
                                results.close();
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
            }
        }
    }


    public void setUploaderListener(InstanceUploaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }


    public static void copyToBytes(InputStream input, OutputStream output,
            int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int bytesRead = input.read(buf);
        while (bytesRead != -1) {
            output.write(buf, 0, bytesRead);
            bytesRead = input.read(buf);
        }
        output.flush();
    }

}
