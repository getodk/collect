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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for downloading a given list of forms. We assume right now that the forms are
 * coming from the same server that presented the form list, but theoretically that won't always be
 * true.
 * 
 * @author msundt
 * @author carlhartung
 */
public class DownloadFormsTask extends
        AsyncTask<ArrayList<FormDetails>, String, HashMap<FormDetails, String>> {

    private static final String t = "DownloadFormsTask";

    private static final String MD5_COLON_PREFIX = "md5:";

    private FormDownloaderListener mStateListener;

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
        "http://openrosa.org/xforms/xformsManifest";

    private boolean isXformsManifestNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }


    @Override
    protected HashMap<FormDetails, String> doInBackground(ArrayList<FormDetails>... values) {
        ArrayList<FormDetails> toDownload = values[0];

        int total = toDownload.size();
        int count = 1;
    	Collect.getInstance().getActivityLogger().logAction(this, "downloadForms", String.valueOf(total));

        HashMap<FormDetails, String> result = new HashMap<FormDetails, String>();

        for (int i = 0; i < total; i++) {
            FormDetails fd = toDownload.get(i);
            publishProgress(fd.formName, Integer.valueOf(count).toString(), Integer.valueOf(total)
                    .toString());

            String message = "";

            try {
                // get the xml file
                // if we've downloaded a duplicate, this gives us the file
                File dl = downloadXform(fd.formName, fd.downloadUrl);

                Cursor alreadyExists = null;
                Uri uri = null;
                try {
                    String[] projection = {
                            FormsColumns._ID, FormsColumns.FORM_FILE_PATH
                    };
                    String[] selectionArgs = {
                        dl.getAbsolutePath()
                    };
                    String selection = FormsColumns.FORM_FILE_PATH + "=?";
                    alreadyExists = Collect.getInstance()
                            .getContentResolver()
                            .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs,
                                null);
	
	                if (alreadyExists.getCount() <= 0) {
	                    // doesn't exist, so insert it
	                    ContentValues v = new ContentValues();
	                    v.put(FormsColumns.FORM_FILE_PATH, dl.getAbsolutePath());
	
	                    HashMap<String, String> formInfo = FileUtils.parseXML(dl);
	                    v.put(FormsColumns.DISPLAY_NAME, formInfo.get(FileUtils.TITLE));
	                    v.put(FormsColumns.JR_VERSION, formInfo.get(FileUtils.VERSION));
	                    v.put(FormsColumns.JR_FORM_ID, formInfo.get(FileUtils.FORMID));
	                    v.put(FormsColumns.SUBMISSION_URI, formInfo.get(FileUtils.SUBMISSIONURI));
	                    v.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY));
	                    uri =
	                        Collect.getInstance().getContentResolver()
	                                .insert(FormsColumns.CONTENT_URI, v);
	                	Collect.getInstance().getActivityLogger().logAction(this, "insert", dl.getAbsolutePath());

	                } else {
	                    alreadyExists.moveToFirst();
	                    uri =
	                        Uri.withAppendedPath(FormsColumns.CONTENT_URI,
	                            alreadyExists.getString(alreadyExists.getColumnIndex(FormsColumns._ID)));
	                	Collect.getInstance().getActivityLogger().logAction(this, "refresh", dl.getAbsolutePath());
	                }
                } finally {
                	if ( alreadyExists != null ) {
                		alreadyExists.close();
                	}
                }

                if (fd.manifestUrl != null) {
                	String formMediaPath = null;
                	Cursor c = null;
                	try {
                		c = Collect.getInstance().getContentResolver()
                                .query(uri, null, null, null, null);
	                    if (c.getCount() > 0) {
	                        // should be exactly 1
	                        c.moveToFirst();
	                        formMediaPath = c.getString(c.getColumnIndex(FormsColumns.FORM_MEDIA_PATH));
	                    }
                	} finally {
                		if ( c != null ) {
                			c.close();
                		}
                	}
                	
                	if ( formMediaPath != null ) {
                        String error =
                            downloadManifestAndMediaFiles(formMediaPath, fd,
                                count, total);
                        if (error != null) {
                            message += error;
                        }
                    }
                } else {
                    Log.i(t, "No Manifest for: " + fd.formName);
                }
            } catch (SocketTimeoutException se) {
                se.printStackTrace();
                message += se.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() != null) {
                    message += e.getCause().getMessage();
                } else {
                    message += e.getMessage();
                }
            }
            count++;
            if (message.equalsIgnoreCase("")) {
                message = Collect.getInstance().getString(R.string.success);
            }
            result.put(fd, message);
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

        // proposed name of xml file...
        String path = Collect.FORMS_PATH + File.separator + rootName + ".xml";
        int i = 2;
        f = new File(path);
        while (f.exists()) {
            path = Collect.FORMS_PATH + File.separator + rootName + "_" + i + ".xml";
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

        Cursor c = null;
        try {
        	c = Collect.getInstance().getContentResolver()
                    .query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, null);
	        if (c.getCount() > 0) {
	            // Should be at most, 1
	            c.moveToFirst();
	
	            // delete the file we just downloaded, because it's a duplicate
	            f.delete();
	
	            // set the file returned to the file we already had
	            f = new File(c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
	        }
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }

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
            // assume the downloadUrl is escaped properly
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }

        // WiFi network connections can be renegotiated during a large form download sequence.
        // This will cause intermittent download failures.  Silently retry once after each 
        // failure.  Only if there are two consecutive failures, do we abort.
        boolean success = false;
        int attemptCount = 0;
        final int MAX_ATTEMPT_COUNT = 2;
        while ( !success && ++attemptCount <= MAX_ATTEMPT_COUNT ) {
	        // get shared HttpContext so that authentication and cookies are retained.
	        HttpContext localContext = Collect.getInstance().getHttpContext();
	
	        HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
	
	        // set up request...
	        HttpGet req = WebUtils.createOpenRosaHttpGet(uri);
	
	        HttpResponse response = null;
	        try {
	            response = httpclient.execute(req, localContext);
	            int statusCode = response.getStatusLine().getStatusCode();
	
	            if (statusCode != HttpStatus.SC_OK) {
	            	WebUtils.discardEntityBytes(response);
	            	if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
	            		// clear the cookies -- should not be necessary?
	            		Collect.getInstance().getCookieStore().clear();
	            	}
	                String errMsg =
	                    Collect.getInstance().getString(R.string.file_fetch_failed, downloadUrl,
	                        response.getStatusLine().getReasonPhrase(), statusCode);
	                Log.e(t, errMsg);
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
	                success = true;
	            } finally {
	                if (os != null) {
	                    try {
	                        os.close();
	                    } catch (Exception e) {
	                    }
	                }
	                if (is != null) {
	                	try {
	                		// ensure stream is consumed...
	                        final long count = 1024L;
	                        while (is.skip(count) == count)
	                            ;
	                	} catch (Exception e) {
	                		// no-op
	                	}
	                    try {
	                        is.close();
	                    } catch (Exception e) {
	                    }
	                }
	            }
	
	        } catch (Exception e) {
	        	Log.e(t, e.toString());
	            e.printStackTrace();
	            // silently retry unless this is the last attempt, 
	            // in which case we rethrow the exception.
	            if ( attemptCount == MAX_ATTEMPT_COUNT ) {
	            	throw e;
	            }
	        }
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

        publishProgress(Collect.getInstance().getString(R.string.fetching_manifest, fd.formName),
            Integer.valueOf(count).toString(), Integer.valueOf(total).toString());

        List<MediaFile> files = new ArrayList<MediaFile>();
        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();

        HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);

        DocumentFetchResult result =
            WebUtils.getXmlDocument(fd.manifestUrl, localContext, httpclient);

        if (result.errorMessage != null) {
            return result.errorMessage;
        }

        String errMessage = Collect.getInstance().getString(R.string.access_error, fd.manifestUrl);

        if (!result.isOpenRosaResponse) {
            errMessage += Collect.getInstance().getString(R.string.manifest_server_error);
            Log.e(t, errMessage);
            return errMessage;
        }

        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = result.doc.getRootElement();
        if (!manifestElement.getName().equals("manifest")) {
            errMessage +=
                Collect.getInstance().getString(R.string.root_element_error,
                    manifestElement.getName());
            Log.e(t, errMessage);
            return errMessage;
        }
        String namespace = manifestElement.getNamespace();
        if (!isXformsManifestNamespacedElement(manifestElement)) {
            errMessage += Collect.getInstance().getString(R.string.root_namespace_error, namespace);
            Log.e(t, errMessage);
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
                    errMessage +=
                        Collect.getInstance().getString(R.string.manifest_tag_error,
                            Integer.toString(i));
                    Log.e(t, errMessage);
                    return errMessage;
                }
                files.add(new MediaFile(filename, hash, downloadUrl));
            }
        }

        // OK we now have the full set of files to download...
        Log.i(t, "Downloading " + files.size() + " media files.");
        int mediaCount = 0;
        if (files.size() > 0) {
            FileUtils.createFolder(mediaPath);
            File mediaDir = new File(mediaPath);
            for (MediaFile toDownload : files) {
                if (isCancelled()) {
                    return "cancelled";
                }
                ++mediaCount;
                publishProgress(
                    Collect.getInstance().getString(R.string.form_download_progress, fd.formName,
                        mediaCount, files.size()), Integer.valueOf(count).toString(), Integer
                            .valueOf(total).toString());
                try {
                    File mediaFile = new File(mediaDir, toDownload.filename);

                    if (!mediaFile.exists()) {
                        downloadFile(mediaFile, toDownload.downloadUrl);
                    } else {
                        String currentFileHash = FileUtils.getMd5Hash(mediaFile);
                        String downloadFileHash = toDownload.hash.substring(MD5_COLON_PREFIX.length());

                        if (!currentFileHash.contentEquals(downloadFileHash)) {
                            // if the hashes match, it's the same file
                            // otherwise delete our current one and replace it with the new one
                            mediaFile.delete();
                            downloadFile(mediaFile, toDownload.downloadUrl);
                        } else {
                            // exists, and the hash is the same
                            // no need to download it again
                        	Log.i(t, "Skipping media file fetch -- file hashes identical: " + mediaFile.getAbsolutePath());
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
    protected void onPostExecute(HashMap<FormDetails, String> value) {
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
                mStateListener.progressUpdate(values[0], 
                	Integer.valueOf(values[1]),
                    Integer.valueOf(values[2]));
            }
        }

    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

}
