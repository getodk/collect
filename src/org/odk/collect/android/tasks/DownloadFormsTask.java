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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Background task for downloading forms from urls or a formlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a form list. If a hashmap
 * containing form/url pairs is passed, we download those forms.
 * 
 * @author carlhartung
 */
public class DownloadFormsTask extends AsyncTask<ArrayList<FormDetails>, String, String> {

    private static final String t = "DownlaodFormsTask";

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final String MD5_COLON_PREFIX = "md5:";

    private FormDownloaderListener mStateListener;

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
        "http://openrosa.org/xforms/xformsManifest";


    private boolean isXformsManifestNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }

    private static class DocumentFetchResult {
        public final String errorMessage;
        public final Document doc;
        public final boolean isOpenRosaResponse;


        DocumentFetchResult(String msg) {
            errorMessage = msg;
            doc = null;
            isOpenRosaResponse = false;
        }


        DocumentFetchResult(Document doc, boolean isOpenRosaResponse) {
            errorMessage = null;
            this.doc = doc;
            this.isOpenRosaResponse = isOpenRosaResponse;
        }
    }


    /**
     * Common method for returning a parsed xml document given a url and the http context and client
     * objects involved in the web connection.
     * 
     * @param urlString
     * @param localContext
     * @param httpclient
     * @return
     */
    private DocumentFetchResult getXmlDocument(String urlString, HttpContext localContext,
            HttpClient httpclient, int fetch_doc_failed, int fetch_doc_failed_no_detail) {
        URI u = null;
        try {
            URL url = new URL(urlString);
            u = url.toURI();
        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentFetchResult(e.getLocalizedMessage()
            // + app.getString(R.string.while_accessing) + urlString);
                    + ("while accessing") + urlString);
        }

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(u);

        HttpResponse response = null;
        try {
            response = httpclient.execute(req, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();

            if (entity != null
                    && (statusCode != 200 || !entity.getContentType().getValue().toLowerCase()
                            .contains(HTTP_CONTENT_TYPE_TEXT_XML))) {
                try {
                    // don't really care about the stream...
                    InputStream is = response.getEntity().getContent();
                    // read to end of stream...
                    final long count = 1024L;
                    while (is.skip(count) == count)
                        ;
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (statusCode != 200) {
                String webError =
                    response.getStatusLine().getReasonPhrase() + " (" + statusCode + ")";

                return new DocumentFetchResult(
                // app.getString(fetch_doc_failed)
                        "fetch doc failed 1" + webError
                        // + app.getString(R.string.while_accessing) + u.toString()
                        // + app.getString(R.string.network_login_failure));
                                + "while accessing" + u.toString() + "network login failure");
            }

            if (entity == null) {
                Log.e(t, "No entity body returned from: " + u.toString() + " is not text/xml");
                return new DocumentFetchResult(
                // app.getString(fetch_doc_failed_no_detail)
                        "fetch doc failed no detail"
                        // + app.getString(R.string.while_accessing) + u.toString()
                        // + app.getString(R.string.network_login_failure));
                                + "while accessing" + u.toString() + "network login failure");
            }

            if (!entity.getContentType().getValue().toLowerCase()
                    .contains(HTTP_CONTENT_TYPE_TEXT_XML)) {
                Log.e(t, "ContentType: " + entity.getContentType().getValue() + "returned from: "
                        + u.toString() + " is not text/xml");
                return new DocumentFetchResult(
                // app.getString(fetch_doc_failed_no_detail)
                        "fetch doc failed no detail 2"
                        // + app.getString(R.string.while_accessing) + u.toString()
                        // + app.getString(R.string.network_login_failure));
                                + "while accessing" + u.toString() + "network login failure");
            }

            // parse response
            Document doc = null;
            try {
                InputStream is = null;
                InputStreamReader isr = null;
                try {
                    is = entity.getContent();
                    isr = new InputStreamReader(is, "UTF-8");
                    doc = new Document();
                    KXmlParser parser = new KXmlParser();
                    parser.setInput(isr);
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    doc.parse(parser);
                    isr.close();
                } finally {
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e) {
                            // no-op
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            // no-op
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(t, "Parsing failed with " + e.getMessage());
                return new DocumentFetchResult(
                // app.getString(fetch_doc_failed_no_detail)
                        "fetch doc failed no detail 3"
                        // + app.getString(R.string.while_accessing) + u.toString());
                                + "while accessing " + u.toString());
            }

            boolean isOR = false;
            Header[] fields = response.getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
            if (fields != null && fields.length >= 1) {
                isOR = true;
                boolean versionMatch = false;
                boolean first = true;
                StringBuilder b = new StringBuilder();
                for (Header h : fields) {
                    if (WebUtils.OPEN_ROSA_VERSION.equals(h.getValue())) {
                        versionMatch = true;
                        break;
                    }
                    if (!first) {
                        b.append("; ");
                    }
                    first = false;
                    b.append(h.getValue());
                }
                if (!versionMatch) {
                    Log.w(
                        t,
                        WebUtils.OPEN_ROSA_VERSION_HEADER + " unrecognized version(s): "
                                + b.toString());
                }
            }
            return new DocumentFetchResult(doc, isOR);
        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentFetchResult(
            // app.getString(fetch_doc_failed)
                    "fetch doc failed 2"
                    // + e.getLocalizedMessage() + app.getString(R.string.while_accessing)
                            + e.getLocalizedMessage() + "while accessing" + u.toString());
        }
    }


    @Override
    protected String doInBackground(ArrayList<FormDetails>... values) {
        ArrayList<FormDetails> toDownload = values[0];

        String result = "";
        int total = toDownload.size();
        int count = 1;

        for (int i = 0; i < total; i++) {
            FormDetails fd = toDownload.get(i);
            publishProgress(fd.formName, Integer.valueOf(count).toString(), Integer.valueOf(total)
                    .toString());
            try {
                // get the xml file
                File dl = downloadXform(fd.formName, fd.downloadUrl);

                String[] projection = {
                        FormsColumns._ID, FormsColumns.FORM_FILE_PATH
                };
                String[] selectionArgs = {
                    dl.getAbsolutePath()
                };
                String selection = FormsColumns.FORM_FILE_PATH + "=?";
                Cursor alreadyExists =
                    Collect.getInstance()
                            .getContentResolver()
                            .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs,
                                null);

                Uri uri = null;
                if (!(alreadyExists.getCount() > 0)) {
                    // doesn't exist, so insert it

                    ContentValues v = new ContentValues();
                    v.put(FormsColumns.FORM_FILE_PATH, dl.getAbsolutePath());
                    v.put(FormsColumns.DISPLAY_NAME, fd.formName);
                    v.put(FormsColumns.JR_FORM_ID, fd.formID);
                    uri =
                        Collect.getInstance().getContentResolver()
                                .insert(FormsColumns.CONTENT_URI, v);
                } else {
                    alreadyExists.moveToFirst();
                    uri =
                        Uri.withAppendedPath(FormsColumns.CONTENT_URI,
                            alreadyExists.getString(alreadyExists.getColumnIndex(FormsColumns._ID)));
                }

                if (fd.manifestUrl != null) {
                    Cursor c =
                        Collect.getInstance().getContentResolver()
                                .query(uri, null, null, null, null);
                    if (c.getCount() > 0) {
                        // should be exactly 1
                        c.moveToFirst();

                        String error =
                            downloadManifestAndMediaFiles(
                                c.getString(c.getColumnIndex(FormsColumns.FORM_MEDIA_PATH)), fd,
                                count, total);
                        if (error != null) {
                            result = error;
                        }
                    }
                } else {
                    //TODO:  manifest was null?                
                    }
            } catch (SocketTimeoutException se) {
                se.printStackTrace();
                result = "socket timeout exception";
                // result.put(DL_FORM, new FormDetails(fd.formName));
                // result.put(DL_ERROR_MSG, new FormDetails(app.getString(R.string.timeout_error)
                // + se.getLocalizedMessage() + app.getString(R.string.while_accessing)
                // + fd.downloadUrl + app.getString(R.string.network_login_failure)));
                break;
            } catch (Exception e) {
                e.printStackTrace();
                result = "exeception e";
                // result.put(DL_FORM, new FormDetails(fd.formName));
                // result.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()
                // + app.getString(R.string.while_accessing) + fd.downloadUrl
                // + app.getString(R.string.network_login_failure)));
                break;
            }
            count++;
        }

        return result;
    }


    /**
     * Takes the formName and the URL and attempts to download the specified file. Returns a file
     * object representing the downloaded file.
     * 
     * @param formName
     * @param url
     * @return
     * @throws Exception
     */
    private File downloadXform(String formName, String url) throws Exception {

        File f = null;

        // clean up friendly form name...
        String rootName = formName.replaceAll("[^\\p{L}\\p{Digit}]", " ");
        rootName = rootName.replaceAll("\\p{javaWhitespace}+", " ");
        rootName = rootName.trim();
        FileUtils.createFolder(FileUtils.FORMS_PATH);
        // proposed name of xml file...
        String path = FileUtils.FORMS_PATH + rootName + ".xml";
        int i = 2;
        f = new File(path);
        while (f.exists()) {
            path = FileUtils.FORMS_PATH + rootName + "_" + i + ".xml";
            f = new File(path);
            i++;
        }

        downloadFile(f, url);

        // we've downloaded the file, and we may have renamed it
        // make sure it's not the same as a file we already have
        String[] projection = {
            FormsColumns.FORM_FILE_PATH
        };
        String[] selectionArgs = {
            FileUtils.getMd5Hash(f)
        };
        String selection = FormsColumns.MD5_HASH + "=?";

        Cursor c =
            Collect.getInstance().getContentResolver()
                    .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        if (c.getCount() > 0) {
            // Should be at most, 1
            c.moveToFirst();

            // delete the file we just downloaded, because it's a duplicate
            f.delete();

            // set the file returned to the file we already had
            f = new File(c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
        }
        c.close();

        return f;
    }


    /**
     * Common routine to download a document from the downloadUrl and save the contents in the file
     * 'f'. Shared by media file download and form file download.
     * 
     * @param f
     * @param downloadUrl
     * @throws Exception
     */
    private void downloadFile(File f, String downloadUrl) throws Exception {
        URI uri = null;
        try {
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();

        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(uri);

        HttpResponse response = null;
        try {
            response = httpclient.execute(req, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                String errMsg =
                    "fetch file failed; " + f.getAbsolutePath()
                            + response.getStatusLine().getReasonPhrase() + " (" + statusCode + ")";
                Log.e(t, "Fetch of " + f.getAbsolutePath() + " failed: "
                        + response.getStatusLine().getReasonPhrase() + " (" + statusCode + ")");
                throw new Exception(errMsg);
            }

            // write connection to file
            InputStream is = null;
            OutputStream os = null;
            try {
                is = response.getEntity().getContent();
                os = new FileOutputStream(f);
                byte buf[] = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    os.write(buf, 0, len);
                }
                os.flush();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static class MediaFile {
        final String filename;
        final String hash;
        final String downloadUrl;


        MediaFile(String filename, String hash, String downloadUrl) {
            this.filename = filename;
            this.hash = hash;
            this.downloadUrl = downloadUrl;
        }
    }


    private String downloadManifestAndMediaFiles(String mediaPath, FormDetails fd, int count,
            int total) {
        if (fd.manifestUrl == null)
            return null;

        publishProgress(fd.formName + " getting manifest ", Integer.valueOf(count).toString(),
            Integer.valueOf(total).toString());

        List<MediaFile> files = new ArrayList<MediaFile>();
        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();

        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        DocumentFetchResult result =
            getXmlDocument(fd.manifestUrl, localContext, httpclient, 555, 777);

        if (result.errorMessage != null) {
            return result.errorMessage;
        }

        String errMessage =
        // app.getString(R.string.fetch_manifest_failed_no_detail)
        // + app.getString(R.string.while_accessing)
            "manifest failed no detail 1 " + "while accessing " + fd.manifestUrl;

        if (!result.isOpenRosaResponse) {
            Log.e(t, "Manifest reply doesn't report an OpenRosa version -- bad server?");
            return errMessage;
        }

        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = result.doc.getRootElement();
        if (!manifestElement.getName().equals("manifest")) {
            Log.e(t, "Root element is not <manifest> -- was " + manifestElement.getName());
            return errMessage;
        }
        String namespace = manifestElement.getNamespace();
        if (!isXformsManifestNamespacedElement(manifestElement)) {
            Log.e(t, "Root element Namespace is incorrect: " + namespace);
            return errMessage;
        }
        int nElements = manifestElement.getChildCount();
        for (int i = 0; i < nElements; ++i) {
            if (manifestElement.getType(i) != Element.ELEMENT) {
                // e.g., whitespace (text)
                continue;
            }
            Element mediaFileElement = (Element) manifestElement.getElement(i);
            if (!isXformsManifestNamespacedElement(mediaFileElement)) {
                // someone else's extension?
                continue;
            }
            String name = mediaFileElement.getName();
            if (name.equalsIgnoreCase("mediaFile")) {
                String filename = null;
                String hash = null;
                String downloadUrl = null;
                // don't process descriptionUrl
                int childCount = mediaFileElement.getChildCount();
                for (int j = 0; j < childCount; ++j) {
                    if (mediaFileElement.getType(j) != Element.ELEMENT) {
                        // e.g., whitespace (text)
                        continue;
                    }
                    Element child = mediaFileElement.getElement(j);
                    if (!isXformsManifestNamespacedElement(child)) {
                        // someone else's extension?
                        continue;
                    }
                    String tag = child.getName();
                    if (tag.equals("filename")) {
                        filename = XFormParser.getXMLText(child, true);
                        if (filename != null && filename.length() == 0) {
                            filename = null;
                        }
                    } else if (tag.equals("hash")) {
                        hash = XFormParser.getXMLText(child, true);
                        if (hash != null && hash.length() == 0) {
                            hash = null;
                        }
                    } else if (tag.equals("downloadUrl")) {
                        downloadUrl = XFormParser.getXMLText(child, true);
                        if (downloadUrl != null && downloadUrl.length() == 0) {
                            downloadUrl = null;
                        }
                    }
                }
                if (filename == null || downloadUrl == null || hash == null) {
                    Log.e(t, "Manifest entry " + Integer.toString(i)
                            + " is missing one or more tags: filename, hash, or downloadUrl");
                    return errMessage;
                }
                files.add(new MediaFile(filename, hash, downloadUrl));
            }
        }

        // OK we now have the full set of files to download...
        Log.i(t, "Downloading " + files.size() + " media files.");
        int mCount = 0;
        if (files.size() > 0) {
            FileUtils.createFolder(mediaPath);
            File mediaDir = new File(mediaPath);
            for (MediaFile toDownload : files) {
                if (isCancelled()) {
                    return "cancelled";
                }
                ++mCount;
                publishProgress(fd.formName + "getting media files count: " + mCount + " and size "
                        + files.size(), Integer.valueOf(count).toString(), Integer.valueOf(total)
                        .toString());
                try {
                    File mediaFile = new File(mediaDir, toDownload.filename);

                    String currentFileHash = FileUtils.getMd5Hash(mediaFile);
                    String downloadFileHash = toDownload.hash.substring(MD5_COLON_PREFIX.length());

                    if (!mediaFile.exists()) {
                        downloadFile(mediaFile, toDownload.downloadUrl);
                    } else {
                        if (!currentFileHash.contentEquals(downloadFileHash)) {
                            // if the hashes match, it's the same file
                            // otherwise delete our current one and replace it with the new one
                            mediaFile.delete();
                            downloadFile(mediaFile, toDownload.downloadUrl);
                        } else {
                            // exists, and the hash is the same
                            // no need to download it again
                        }
                    }
                } catch (Exception e) {
                    return e.getLocalizedMessage();
                }
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(String value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.formsDownloadingComplete(value);
            }
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0], new Integer(values[1]).intValue(),
                    new Integer(values[2]).intValue());
            }
        }

    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

}
