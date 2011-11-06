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

package org.odk.collect.android.utilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.application.Collect;
import org.xmlpull.v1.XmlPullParser;

import android.text.format.DateFormat;
import android.util.Log;

/**
 * Common utility methods for managing the credentials associated with the request context and
 * constructing http context, client and request with the proper parameters and OpenRosa headers.
 * 
 * @author mitchellsundt@gmail.com
 */
public final class WebUtils {
    public static final String t = "WebUtils";

    public static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    public static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";

    public static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";
    public static final int CONNECTION_TIMEOUT = 30000;

    private static final GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));


    public static final List<AuthScope> buildAuthScopes(String host) {
        List<AuthScope> asList = new ArrayList<AuthScope>();

        AuthScope a;
        // allow digest auth on any port...
        a = new AuthScope(host, -1, null, AuthPolicy.DIGEST);
        asList.add(a);
        // and allow basic auth on the standard TLS/SSL ports...
        a = new AuthScope(host, 443, null, AuthPolicy.BASIC);
        asList.add(a);
        a = new AuthScope(host, 8443, null, AuthPolicy.BASIC);
        asList.add(a);

        return asList;
    }


    public static final void clearAllCredentials() {
        HttpContext localContext = Collect.getInstance().getHttpContext();
        CredentialsProvider credsProvider =
            (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);
        credsProvider.clear();
    }


    public static final boolean hasCredentials(String userEmail, String host) {
        HttpContext localContext = Collect.getInstance().getHttpContext();
        CredentialsProvider credsProvider =
            (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);

        List<AuthScope> asList = buildAuthScopes(host);
        boolean hasCreds = true;
        for (AuthScope a : asList) {
            Credentials c = credsProvider.getCredentials(a);
            if (c == null) {
                hasCreds = false;
                continue;
            }
        }
        return hasCreds;
    }


    public static final void addCredentials(String userEmail, String password, String host) {
        HttpContext localContext = Collect.getInstance().getHttpContext();
        Credentials c = new UsernamePasswordCredentials(userEmail, password);
        addCredentials(localContext, c, host);
    }


    private static final void addCredentials(HttpContext localContext, Credentials c, String host) {
        CredentialsProvider credsProvider =
            (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);

        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, c);
        }
    }


    private static final void setOpenRosaHeaders(HttpRequest req) {
        req.setHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION);
        g.setTime(new Date());
        req.setHeader(DATE_HEADER, DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString());
    }


    public static final HttpHead createOpenRosaHttpHead(URI uri) {
        HttpHead req = new HttpHead(uri);
        setOpenRosaHeaders(req);
        return req;
    }


    public static final HttpGet createOpenRosaHttpGet(URI uri) {
        HttpGet req = new HttpGet();
        setOpenRosaHeaders(req);
        req.setURI(uri);
        return req;
    }


    public static final HttpPost createOpenRosaHttpPost(URI uri) {
        HttpPost req = new HttpPost(uri);
        setOpenRosaHeaders(req);
        return req;
    }


    public static final HttpClient createHttpClient(int timeout) {
        // configure connection
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        // support redirecting to handle http: => https: transition
        HttpClientParams.setRedirecting(params, true);
        // support authenticating
        HttpClientParams.setAuthenticating(params, true);
        // if possible, bias toward digest auth (may not be in 4.0 beta 2)
        List<String> authPref = new ArrayList<String>();
        authPref.add(AuthPolicy.DIGEST);
        authPref.add(AuthPolicy.BASIC);
        // does this work in Google's 4.0 beta 2 snapshot?
        params.setParameter("http.auth-target.scheme-pref", authPref);

        // setup client
        HttpClient httpclient = new DefaultHttpClient(params);
        httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 1);
        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        return httpclient;
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
    public static DocumentFetchResult getXmlDocument(String urlString, HttpContext localContext,
            HttpClient httpclient) {
        URI u = null;
        try {
            URL url = new URL(URLDecoder.decode(urlString, "utf-8"));
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
                            .contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML))) {
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

                return new DocumentFetchResult(u.toString() + " responded with: " + webError,
                        statusCode);
            }

            if (entity == null) {
                String error = "No entity body returned from: " + u.toString();
                Log.e(t, error);
                return new DocumentFetchResult(error, 0);
            }

            if (!entity.getContentType().getValue().toLowerCase()
                    .contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML)) {
                String error =
                    "ContentType: "
                            + entity.getContentType().getValue()
                            + " returned from: "
                            + u.toString()
                            + " is not text/xml.  This is often caused a network proxy.  Do you need to login to your network?";
                Log.e(t, error);
                return new DocumentFetchResult(error, 0);
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
                String error =
                    "Parsing failed with " + e.getMessage() + "while accessing " + u.toString();
                Log.e(t, error);
                return new DocumentFetchResult(error, 0);
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
            String cause;
            if (e.getCause() != null) {
                cause = e.getCause().getMessage();
            } else {
                cause = e.getMessage();
            }
            String error = "Error: " + cause + " while accessing " + u.toString();

            Log.w(t, error);
            return new DocumentFetchResult(error, 0);
        }
    }
}
