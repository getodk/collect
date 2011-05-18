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
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.WebUtils;
import org.xmlpull.v1.XmlPullParser;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

/**
 * Background task for downloading forms from urls or a formlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a form list. If a hashmap
 * containing form/url pairs is passed, we download those forms.
 * 
 * @author carlhartung
 */
public class DownloadFormListTask extends AsyncTask<Void, String, HashMap<String, FormDetails>> {

    private static final String t = "DownloadFormsTask";
    // used to store form name if one errors
    public static final String DL_FORM = "dlform";

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";

    // used to indicate that we tried to download forms. If it's not included we tried to download a
    // form list.
    public static final String DL_FORMS = "dlforms";

    private static final int CONNECTION_TIMEOUT = 30000;

    private FormListDownloaderListener mStateListener;
    private ContentResolver mContentResolver;
    
    private String mErrorMessage;
    private int mErrorCode;

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
        "http://openrosa.org/xforms/xformsManifest";

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST =
        "http://openrosa.org/xforms/xformsList";


    public DownloadFormListTask(ContentResolver cr) {
        mContentResolver = cr;
    }

    private static class DocumentFetchResult {
        public final String errorMessage;
        public final int responseCode;
        public final Document doc;
        public final boolean isOpenRosaResponse;


        DocumentFetchResult(String msg, int response) {
            responseCode = response;
            errorMessage = msg;
            doc = null;
            isOpenRosaResponse = false;
        }


        DocumentFetchResult(Document doc, boolean isOpenRosaResponse) {
            responseCode = 0;
            errorMessage = null;
            this.doc = doc;
            this.isOpenRosaResponse = isOpenRosaResponse;
        }
    }


    private boolean isXformsListNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST);
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
            HttpClient httpclient) {
        URI u = null;
        try {
            URL url = new URL(urlString);
            u = url.toURI();
        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentFetchResult(e.getLocalizedMessage()
            // + app.getString(R.string.while_accessing) + urlString);
                    + ("while accessing") + urlString, 0);
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
                                + "while accessing" + u.toString() + "network login failure", statusCode);
            }

            if (entity == null) {
                Log.e(t, "No entity body returned from: " + u.toString() + " is not text/xml");
                return new DocumentFetchResult(
                // app.getString(fetch_doc_failed_no_detail)
                        "fetch doc failed no detail"
                        // + app.getString(R.string.while_accessing) + u.toString()
                        // + app.getString(R.string.network_login_failure));
                                + "while accessing" + u.toString() + "network login failure", 0);
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
                                + "while accessing" + u.toString() + "network login failure", 0);
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
                                + "while accessing " + u.toString(), 0);
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
                            + e.getLocalizedMessage() + "while accessing" + u.toString(), 0);
        }
    }


    @Override
    protected HashMap<String, FormDetails> doInBackground(Void... values) {

        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        String downloadListUrl = settings.getString(PreferencesActivity.KEY_SERVER_URL,
                Collect.getInstance().getString(R.string.default_server_url)) + "/formList";



            // This gets a list of available forms from the specified server.
            HashMap<String, FormDetails> formList = new HashMap<String, FormDetails>();

            // get shared HttpContext so that authentication and cookies are retained.
            HttpContext localContext = Collect.getInstance().getHttpContext();

            HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

            DocumentFetchResult result =
                getXmlDocument(downloadListUrl, localContext, httpclient);
            // R.string.fetch_formlist_failed, R.string.fetch_formlist_failed_no_detail);

            if (result.errorMessage != null) {
                mErrorCode = result.responseCode;
                mErrorMessage = result.errorMessage;
                cancel(true);
                return null;
            }

            if (result.isOpenRosaResponse) {
                // Attempt OpenRosa 1.0 parsing
                Element xformsElement = result.doc.getRootElement();
                if (!xformsElement.getName().equals("xforms")) {
                    Log.e(t, "Parsing OpenRosa reply -- root element is not <xforms> :"
                            + xformsElement.getName());
                    formList.put(DL_ERROR_MSG, new FormDetails(
                    // app.getString(R.string.parse_openrosa_formlist_failed)
                            "parse OR formlist failed"));
                    return formList;
                }
                String namespace = xformsElement.getNamespace();
                if (!isXformsListNamespacedElement(xformsElement)) {
                    Log.e(t, "Parsing OpenRosa reply -- root element namespace is incorrect:"
                            + namespace);
                    formList.put(DL_ERROR_MSG, new FormDetails(
                    // app.getString(R.string.parse_openrosa_formlist_failed)
                            " parase OR formlist failed 2"));
                    return formList;
                }
                int nElements = xformsElement.getChildCount();
                for (int i = 0; i < nElements; ++i) {
                    if (xformsElement.getType(i) != Element.ELEMENT) {
                        // e.g., whitespace (text)
                        continue;
                    }
                    Element xformElement = (Element) xformsElement.getElement(i);
                    if (!isXformsListNamespacedElement(xformElement)) {
                        // someone else's extension?
                        continue;
                    }
                    String name = xformElement.getName();
                    if (!name.equalsIgnoreCase("xform")) {
                        // someone else's extension?
                        continue;
                    }

                    // this is something we know how to interpret
                    String formId = null;
                    String formName = null;
                    String majorMinorVersion = null;
                    String description = null;
                    String downloadUrl = null;
                    String manifestUrl = null;
                    // don't process descriptionUrl
                    int fieldCount = xformElement.getChildCount();
                    for (int j = 0; j < fieldCount; ++j) {
                        if (xformElement.getType(j) != Element.ELEMENT) {
                            // whitespace
                            continue;
                        }
                        Element child = xformElement.getElement(j);
                        if (!isXformsListNamespacedElement(child)) {
                            // someone else's extension?
                            continue;
                        }
                        String tag = child.getName();
                        if (tag.equals("formID")) {
                            formId = XFormParser.getXMLText(child, true);
                            if (formId != null && formId.length() == 0) {
                                formId = null;
                            }
                        } else if (tag.equals("name")) {
                            formName = XFormParser.getXMLText(child, true);
                            if (formName != null && formName.length() == 0) {
                                formName = null;
                            }
                        } else if (tag.equals("majorMinorVersion")) {
                            majorMinorVersion = XFormParser.getXMLText(child, true);
                            if (majorMinorVersion != null && majorMinorVersion.length() == 0) {
                                majorMinorVersion = null;
                            }
                        } else if (tag.equals("descriptionText")) {
                            description = XFormParser.getXMLText(child, true);
                            if (description != null && description.length() == 0) {
                                description = null;
                            }
                        } else if (tag.equals("downloadUrl")) {
                            downloadUrl = XFormParser.getXMLText(child, true);
                            if (downloadUrl != null && downloadUrl.length() == 0) {
                                downloadUrl = null;
                            }
                        } else if (tag.equals("manifestUrl")) {
                            manifestUrl = XFormParser.getXMLText(child, true);
                            if (manifestUrl != null && manifestUrl.length() == 0) {
                                manifestUrl = null;
                            }
                        }
                    }
                    if (formId == null || downloadUrl == null || formName == null) {
                        Log.e(t,
                            "Parsing OpenRosa reply -- Forms list entry " + Integer.toString(i)
                                    + " is missing one or more tags: formId, name, or downloadUrl");
                        formList.clear();
                        formList.put(DL_ERROR_MSG, new FormDetails(
                        // app.getString(R.string.parse_openrosa_formlist_failed)
                                "prase OR formlist failed 3"));
                        return formList;
                    }
                    Integer modelVersion = null;
                    Integer uiVersion = null;
                    try {
                        if (majorMinorVersion == null || majorMinorVersion.length() == 0) {
                            modelVersion = null;
                            uiVersion = null;
                        } else {
                            int idx = majorMinorVersion.indexOf(".");
                            if (idx == -1) {
                                modelVersion = Integer.parseInt(majorMinorVersion);
                                uiVersion = null;
                            } else {
                                modelVersion =
                                    Integer.parseInt(majorMinorVersion.substring(0, idx));
                                uiVersion =
                                    (idx == majorMinorVersion.length() - 1) ? null : Integer
                                            .parseInt(majorMinorVersion.substring(idx + 1));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(t,
                            "Parsing OpenRosa reply -- Forms list entry " + Integer.toString(i)
                                    + " has an invalid majorMinorVersion: " + majorMinorVersion);
                        formList.clear();
                        formList.put(DL_ERROR_MSG, new FormDetails(
                        // app.getString(R.string.parse_openrosa_formlist_failed)
                                "parse OR formlist failed 4"));
                        return formList;
                    }
                    formList.put(formName, new FormDetails(formName, downloadUrl, manifestUrl, formId));
                }
            } else {
                // Aggregate 0.9.x mode...
                // populate HashMap with form names and urls
                Element formsElement = result.doc.getRootElement();
                int formsCount = formsElement.getChildCount();
                for (int i = 0; i < formsCount; ++i) {
                    if (formsElement.getType(i) != Element.ELEMENT) {
                        // whitespace
                        continue;
                    }
                    Element child = formsElement.getElement(i);
                    String tag = child.getName();
                    String formId = null;
                    if (tag.equals("formID")) {
                        formId = XFormParser.getXMLText(child, true);
                        if (formId != null && formId.length() == 0) {
                            formId = null;
                        }
                    }
                    if (tag.equalsIgnoreCase("form")) {
                        String formName = XFormParser.getXMLText(child, true);
                        if (formName != null && formName.length() == 0) {
                            formName = null;
                        }
                        String downloadUrl = child.getAttributeValue(null, "url");
                        downloadUrl = downloadUrl.trim();
                        if (downloadUrl != null && downloadUrl.length() == 0) {
                            downloadUrl = null;
                        }
                        if (downloadUrl == null || formName == null) {
                            Log.e(t,
                                "Parsing OpenRosa reply -- Forms list entry " + Integer.toString(i)
                                        + " is missing form name or url attribute");
                            formList.clear();
                            formList.put(DL_ERROR_MSG, new FormDetails(
                            // app.getString(R.string.parse_legacy_formlist_failed)
                                    "parse legacy formlist failed"

                            ));
                            return formList;
                        }
                        formList.put(formName, new FormDetails(formName, downloadUrl, null, formId));
                    }
                }
            }
            return formList;
    }


    @Override
    protected void onPostExecute(HashMap<String, FormDetails> value) {
        synchronized (this) {
            if (mStateListener != null) {
                if (isCancelled()) {
                    mStateListener.formListDownloadingError(mErrorCode, mErrorMessage);
                } else {
                    mStateListener.formListDownloadingComplete(value);
                }
            }
        }
    }

//
//    @Override
//    protected void onProgressUpdate(String... values) {
//        synchronized (this) {
//            if (mStateListener != null) {
//                // update progress and total
//                mStateListener.progressUpdate(values[0], new Integer(values[1]).intValue(),
//                    new Integer(values[2]).intValue());
//            }
//        }
//
//    }


    public void setDownloaderListener(FormListDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

   

}
