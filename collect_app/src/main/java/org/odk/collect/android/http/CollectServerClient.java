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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.xmlpull.v1.XmlPullParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Common utility methods for managing the credentials associated with the
 * request context and constructing http context, client and request with the
 * proper parameters and OpenRosa headers.
 *
 * @author mitchellsundt@gmail.com
 */
public class CollectServerClient {

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    protected OpenRosaHttpInterface httpInterface;

    @Inject
    public CollectServerClient(OpenRosaHttpInterface httpInterface) {
        this.httpInterface = httpInterface;
    }


    /**
     * Common method for returning a parsed xml document given a url and the
     * http context and client objects involved in the web connection.
     */
    public DocumentFetchResult getXmlDocument(String urlString) {

        // parse response
        Document doc;

        HttpGetResult inputStreamResult;
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
     * @return HttpGetResult - An object containing the Stream, Hash and Headers
     * @throws Exception - Can throw a multitude of Exceptions, such as MalformedURLException or IOException
     */
    public @NonNull
    HttpGetResult getHttpInputStream(@NonNull String downloadUrl, @Nullable final String contentType) throws Exception {
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

        return httpInterface.get(uri, contentType, WebCredentialsUtils.getInstance().getCredentials(uri));
    }


    public static String getPlainTextMimeType() {
        return "text/plain";
    }


    public static class Outcome {
        public Uri authRequestingServer;
        public HashMap<String, String> messagesByInstanceId = new HashMap<>();
    }

}