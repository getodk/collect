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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HttpContext;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.odk.collect.android.utilities.FilterUtils.FilterCriteria;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Background task for uploading completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderTask extends
        AsyncTask<String, Integer, ArrayList<InstanceUploaderListener.UploadOutcome>> {

    private static String t = "InstanceUploaderTask";
    // private static long MAX_BYTES = 1048576 - 1024; // 1MB less 1KB overhead
    InstanceUploaderListener mStateListener;
    private static final int CONNECTION_TIMEOUT = 30000;


    /**
     * The values are the names of the instances to upload -- i.e., the directory names.
     */
    @Override
    protected ArrayList<InstanceUploaderListener.UploadOutcome> doInBackground(String... values) {
        ArrayList<InstanceUploaderListener.UploadOutcome> uploadOutcome =
            new ArrayList<InstanceUploaderListener.UploadOutcome>();
        int instanceCount = values.length;
        Set<String> instanceDirs = new HashSet<String>();
        instanceDirs.addAll(Arrays.asList(values));

        Collect app = Collect.getInstance();

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = app.getHttpContext();
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        Map<URI, URI> uriRemap = new HashMap<URI, URI>();

        FilterCriteria fc =
            FilterUtils.buildInverseSelectionClause(SubmissionsStorage.KEY_STATUS,
                SubmissionsStorage.STATUS_INCOMPLETE);

        Cursor c = null;
        try {
            c =
                app.getContentResolver().query(
                    SubmissionsStorage.CONTENT_URI_INFO_DATASET,
                    new String[] {
                            SubmissionsStorage.KEY_ID, SubmissionsStorage.KEY_SUBMISSION_URI,
                            SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH
                    }, fc.selection, fc.selectionArgs, null);

            int idxInstanceDir = c.getColumnIndex(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH);
            int idxUri = c.getColumnIndex(SubmissionsStorage.KEY_SUBMISSION_URI);
            int i = 0;
            while (c.moveToNext()) {
                String instanceDir = c.getString(idxInstanceDir);
                if (!instanceDirs.contains(instanceDir))
                    continue;
                ++i;
                String urlString = c.getString(idxUri);
                // it is one of the submissions we want to do...

                URI u = null;
                try {
                    URL url = new URL(urlString);
                    u = url.toURI();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e(t, "Invalid url: " + urlString + " for submission " + instanceDir);
                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir,
                            urlString, e.getLocalizedMessage()));
                    continue;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    Log.e(t, "Invalid uri: " + urlString + " for submission " + instanceDir);
                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir,
                            urlString, e.getLocalizedMessage()));
                    continue;
                }

                boolean openRosaServer = false;
                if (uriRemap.containsKey(u)) {
                    // we already issued a head request and got a response,
                    // so we know the proper URL to send the submission to
                    // and the proper scheme. We also know that it was an
                    // OpenRosa compliant server.
                    openRosaServer = true;
                    u = uriRemap.get(u);
                } else {
                    // we need to issue a head request
                    HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);

                    // prepare response
                    HttpResponse response = null;
                    try {
                        response = httpclient.execute(httpHead, localContext);
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == 204) {
                            Header[] locations = response.getHeaders("Location");
                            if (locations != null && locations.length == 1) {
                                try {
                                    URL url = new URL(locations[0].getValue());
                                    URI uNew = url.toURI();
                                    if (u.getHost().equalsIgnoreCase(uNew.getHost())) {
                                        openRosaServer = true;
                                        // trust the server to tell us a new location
                                        // ... and possibly to use https instead.
                                        uriRemap.put(u, uNew);
                                        u = uNew;
                                    } else {
                                        // Don't follow a redirection attempt to a different host.
                                        // We can't tell if this is a spoof or not.
                                        Log.e(t,
                                            "Unexpected redirection attempt to a different host: "
                                                    + uNew.toString());
                                        uploadOutcome
                                                .add(new InstanceUploaderListener.UploadOutcome(
                                                        instanceDir,
                                                        u,
                                                        app
                                                                .getString(R.string.unexpected_redirection)
                                                                + uNew.toString()));
                                        continue;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(
                                            instanceDir, u, e.getLocalizedMessage()));
                                    continue;
                                }
                            }
                        } else {
                            // may be a server that does not handle HEAD requests
                            try {
                                // don't really care about the stream...
                                InputStream is = response.getEntity().getContent();
                                // read to end of stream...
                                final long count = 1024L;
                                while (is.skip(count) == count)
                                    ;
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.w(t, "Status code on Head request: " + statusCode);
                            if (statusCode >= 200 && statusCode <= 299) {
                                uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(
                                        instanceDir, u, app
                                                .getString(R.string.network_login_failure)));
                                continue;
                            }
                        }
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                        uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir,
                                u, e.getLocalizedMessage()));
                        continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir,
                                u, e.getLocalizedMessage()));
                        continue;
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
                publishProgress(i, instanceCount);

                // get instance file
                File instanceFile = new File(FileUtils.getInstanceFilePath(instanceDir));
                File file = new File(FileUtils.getSubmissionBlobPath(instanceDir));

                String submissionFile = file.getName();
                String xmlInstanceFile = instanceFile.getName();

                if (!file.exists()) {
                    String msg =
                        app.getString(R.string.submission_file_not_found) + file.getAbsolutePath();
                    Log.e(t, msg);
                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir, u,
                            msg));
                    continue;
                }

                // find all files in parent directory
                File[] allFiles = file.getParentFile().listFiles();

                boolean someFilesNotUploaded = false;
                // clean up the list, removing anything that is suspicious
                // or that we won't attempt to upload. For OpenRosa servers,
                // we'll upload just about everything...
                List<File> files = new ArrayList<File>();
                for (File f : allFiles) {
                    String fileName = f.getName();
                    int idx = fileName.lastIndexOf(".");
                    String extension = "";
                    if (idx != -1) {
                        extension = fileName.substring(idx + 1);
                    }
                    if (fileName.startsWith(".")) {
                        // potential Apple file attributes file -- ignore it
                        continue;
                    }
                    if (fileName.equals(submissionFile)) {
                        continue; // this is always added
                    } else if (fileName.equals(xmlInstanceFile)) {
                        continue; // omitted
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
                    } else {
                        Log.w(t, "unrecognized file type " + f.getName());
                        someFilesNotUploaded = true;
                    }
                }

                boolean successfulAttemptSoFar = true;
                StringBuilder b = new StringBuilder();
                boolean first = true;
                int j = 0;
                while (j < files.size() || first) {
                    first = false;

                    HttpPost httppost = WebUtils.createOpenRosaHttpPost(u);

                    MimeTypeMap m = MimeTypeMap.getSingleton();

                    long byteCount = 0L;

                    // mime post
                    MultipartEntity entity = new MultipartEntity();

                    // add the submission file first...
                    FileBody fb = new FileBody(file, "text/xml");
                    entity.addPart("xml_submission_file", fb);
                    Log.i(t, "added xml_submission_file: " + file.getName());
                    byteCount += file.length();

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
                            fb = new FileBody(f, "text/xml");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added xml file " + f.getName());
                        } else if (extension.equals("jpg")) {
                            fb = new FileBody(f, "image/jpeg");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added image file " + f.getName());
                        } else if (extension.equals("3gpp")) {
                            fb = new FileBody(f, "audio/3gpp");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added audio file " + f.getName());
                        } else if (extension.equals("3gp")) {
                            fb = new FileBody(f, "video/3gpp");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added video file " + f.getName());
                        } else if (extension.equals("mp4")) {
                            fb = new FileBody(f, "video/mp4");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added video file " + f.getName());
                        } else if (extension.equals("csv")) {
                            fb = new FileBody(f, "text/csv");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added csv file " + f.getName());
                        } else if (extension.equals("xls")) {
                            fb = new FileBody(f, "application/vnd.ms-excel");
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added xls file " + f.getName());
                        } else if (contentType != null) {
                            fb = new FileBody(f, contentType);
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log.i(t, "added recognized filetype (" + contentType + ") "
                                    + f.getName());
                        } else {
                            contentType = "application/octet-stream";
                            fb = new FileBody(f, contentType);
                            entity.addPart(f.getName(), fb);
                            byteCount += f.length();
                            Log
                                    .w(t, "added unrecognized file (" + contentType + ") "
                                            + f.getName());
                        }

                        // we've added at least one attachment to the request...
                        if (j + 1 < files.size()) {
                            if (byteCount + files.get(j + 1).length() > 10000000L) {
                                // the next file would exceed the 10MB threshold...
                                Log.i(t, "Extremely long post is being split into multiple posts");
                                try {
                                    StringBody sb = new StringBody("yes", Charset.forName("UTF-8"));
                                    entity.addPart("*isIncomplete*", sb);
                                } catch (Exception e) {
                                    e.printStackTrace(); // never happens...
                                }
                                ++j; // advance over the last attachment added...
                                break;
                            }
                        }
                    }

                    httppost.setEntity(entity);

                    // prepare response and return uploaded
                    HttpResponse response = null;
                    try {
                        response = httpclient.execute(httppost, localContext);
                        int responseCode = response.getStatusLine().getStatusCode();
                        Log.i(t, "Response code:" + responseCode);
                        // verify that the response was a 201 or 202.
                        // If it wasn't, the submission has failed.
                        if (responseCode != 201 && responseCode != 202) {
                            if (responseCode == 200) {
                                b.append(app.getString(R.string.network_login_failure));
                            } else {
                                b.append(response.getStatusLine().getReasonPhrase() + " ("
                                        + responseCode + ")");
                            }
                            successfulAttemptSoFar = false;
                        }
                        // read the body of the response (needed before we can reuse connection).
                        InputStream is = null;
                        BufferedReader r = null;
                        try {
                            is = response.getEntity().getContent();
                            r = new BufferedReader(new InputStreamReader(is));
                            String line;
                            while ((line = r.readLine()) != null) {
                                if (responseCode == 201 || responseCode == 202) {
                                    Log.i(t, line);
                                } else {
                                    Log.e(t, line);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            b.append(e.getLocalizedMessage());
                            b.append("\n");
                        } finally {
                            if (r != null) {
                                try {
                                    r.close();
                                } catch (Exception e) {
                                } finally {
                                    r = null;
                                }
                            }
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (Exception e) {
                                } finally {
                                    is = null;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        successfulAttemptSoFar = false;
                        b.append(e.getLocalizedMessage());
                        b.append("\n");
                    }
                }

                // ok, all the parts of the submission were sent...
                // If it wasn't, the submission has failed.
                if (successfulAttemptSoFar && b.length() == 0 && !someFilesNotUploaded) {
                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir));
                } else if (successfulAttemptSoFar && b.length() == 0 && someFilesNotUploaded) {
                    uploadOutcome
                            .add(new InstanceUploaderListener.UploadOutcome(instanceDir, false));
                } else {
                    uploadOutcome.add(new InstanceUploaderListener.UploadOutcome(instanceDir, u, b
                            .toString()));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return uploadOutcome;
    }


    @Override
    protected void onPostExecute(ArrayList<InstanceUploaderListener.UploadOutcome> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.uploadingComplete(value);
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
}
