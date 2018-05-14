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

import android.net.Uri;
import android.text.format.DateFormat;

import org.apache.commons.io.IOUtils;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.opendatakit.httpclientandroidlib.Header;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpHost;
import org.opendatakit.httpclientandroidlib.HttpRequest;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.auth.AuthScope;
import org.opendatakit.httpclientandroidlib.auth.Credentials;
import org.opendatakit.httpclientandroidlib.auth.UsernamePasswordCredentials;
import org.opendatakit.httpclientandroidlib.client.AuthCache;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.config.AuthSchemes;
import org.opendatakit.httpclientandroidlib.client.config.CookieSpecs;
import org.opendatakit.httpclientandroidlib.client.config.RequestConfig;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.client.methods.HttpHead;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.client.protocol.HttpClientContext;
import org.opendatakit.httpclientandroidlib.config.SocketConfig;
import org.opendatakit.httpclientandroidlib.impl.auth.BasicScheme;
import org.opendatakit.httpclientandroidlib.impl.client.BasicAuthCache;
import org.opendatakit.httpclientandroidlib.impl.client.HttpClientBuilder;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import timber.log.Timber;

/**
 * Common utility methods for managing the credentials associated with the
 * request context and constructing http context, client and request with the
 * proper parameters and OpenRosa headers.
 *
 * @author mitchellsundt@gmail.com
 */
public final class WebUtils {

    private static final String USER_AGENT_HEADER = "User-Agent";

    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";
    public static final int CONNECTION_TIMEOUT = 30000;

    public static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    public static final String GZIP_CONTENT_ENCODING = "gzip";

    private WebUtils() {

    }

    private static List<AuthScope> buildAuthScopes(String host) {
        List<AuthScope> asList = new ArrayList<>();

        AuthScope a;
        // allow digest auth on any port...
        a = new AuthScope(host, -1, null, AuthSchemes.DIGEST);
        asList.add(a);
        // and allow basic auth on the standard TLS/SSL ports...
        a = new AuthScope(host, 443, null, AuthSchemes.BASIC);
        asList.add(a);
        a = new AuthScope(host, 8443, null, AuthSchemes.BASIC);
        asList.add(a);

        return asList;
    }

    /**
     * Remove all credentials for accessing the specified host.
     */
    public static void clearHostCredentials(String host) {
        CredentialsProvider credsProvider = Collect.getInstance()
                .getCredentialsProvider();
        Timber.i("clearHostCredentials: %s", host);
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, null);
        }
    }

    /**
     * Remove all credentials for accessing the specified host and, if the
     * username is not null or blank then add a (username, password) credential
     * for accessing this host.
     */
    public static void addCredentials(String username, String password,
                                      String host) {
        // to ensure that this is the only authentication available for this
        // host...
        clearHostCredentials(host);
        if (username != null && username.trim().length() != 0) {
            Timber.i("adding credential for host: %s username:%s", host, username);
            Credentials c = new UsernamePasswordCredentials(username, password);
            addCredentials(c, host);
        }
    }

    private static void addCredentials(Credentials c, String host) {
        CredentialsProvider credsProvider = Collect.getInstance()
                .getCredentialsProvider();
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, c);
        }
    }

    public static void enablePreemptiveBasicAuth(
            HttpContext localContext, String host) {
        AuthCache ac = (AuthCache) localContext
                .getAttribute(HttpClientContext.AUTH_CACHE);
        HttpHost h = new HttpHost(host);
        if (ac == null) {
            ac = new BasicAuthCache();
            localContext.setAttribute(HttpClientContext.AUTH_CACHE, ac);
        }
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            if (a.getScheme().equalsIgnoreCase(AuthSchemes.BASIC)) {
                ac.put(h, new BasicScheme());
            }
        }
    }

    private static void setCollectHeaders(HttpRequest req) {
        String userAgent = String.format("%s %s/%s",
                System.getProperty("http.agent"),
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME);
        req.setHeader(USER_AGENT_HEADER, userAgent);
    }

    private static void setOpenRosaHeaders(HttpRequest req) {
        req.setHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION);
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        g.setTime(new Date());
        req.setHeader(DATE_HEADER,
                DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString());
    }

    public static HttpHead createOpenRosaHttpHead(URI uri) {
        HttpHead req = new HttpHead(uri);
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        return req;
    }

    public static HttpGet createOpenRosaHttpGet(URI uri) {
        HttpGet req = new HttpGet();
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        req.setURI(uri);
        return req;
    }

    public static HttpPost createOpenRosaHttpPost(Uri u) {
        HttpPost req = new HttpPost(URI.create(u.toString()));
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        return req;
    }

    /**
     * Create an httpClient with connection timeouts and other parameters set.
     * Save and reuse the connection manager across invocations (this is what
     * requires synchronized access).
     *
     * @return HttpClient properly configured.
     */
    public static synchronized HttpClient createHttpClient(int timeout) {
        // configure connection
        SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT).setSoTimeout(
                2 * timeout)
                .build();

        // if possible, bias toward digest auth (may not be in 4.0 beta 2)
        List<String> targetPreferredAuthSchemes = new ArrayList<String>();
        targetPreferredAuthSchemes.add(AuthSchemes.DIGEST);
        targetPreferredAuthSchemes.add(AuthSchemes.BASIC);

        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(timeout)
                // support authenticating
                .setAuthenticationEnabled(true)
                // support redirecting to handle http: => https: transition
                .setRedirectsEnabled(true)
                .setMaxRedirects(1)
                .setCircularRedirectsAllowed(true)
                .setTargetPreferredAuthSchemes(targetPreferredAuthSchemes)
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        return HttpClientBuilder.create()
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .build();

    }

    /**
     * Utility to ensure that the entity stream of a response is drained of
     * bytes.
     * Apparently some servers require that we manually read all data from the
     * stream to allow its re-use.  Please add more details or bug ID here if
     * you know them.
     */
    public static void discardEntityBytes(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream is = null;
            try {
                is = response.getEntity().getContent();
                while (is.read() != -1) {
                    // loop until all bytes read
                }
            } catch (Exception e) {
                Timber.i(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Timber.d(e);
                    }
                }
            }
        }
    }

    /**
     * Common method for returning a parsed xml document given a url and the
     * http context and client objects involved in the web connection.
     */
    public static DocumentFetchResult getXmlDocument(String urlString,
                                                     HttpContext localContext, HttpClient httpclient) {
        URI u;
        try {
            URL url = new URL(urlString);
            u = url.toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            Timber.i(e, "Error converting URL %s to uri", urlString);
            return new DocumentFetchResult(e.getLocalizedMessage()
                    // + app.getString(R.string.while_accessing) + urlString);
                    + ("while accessing") + urlString, 0);
        }

        if (u.getHost() == null) {
            return new DocumentFetchResult("Invalid server URL (no hostname): " + urlString, 0);
        }

        // if https then enable preemptive basic auth...
        if (u.getScheme().equals("https")) {
            enablePreemptiveBasicAuth(localContext, u.getHost());
        }

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(u);
        req.addHeader(WebUtils.ACCEPT_ENCODING_HEADER, WebUtils.GZIP_CONTENT_ENCODING);

        HttpResponse response;
        try {
            response = httpclient.execute(req, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();

            if (statusCode != HttpStatus.SC_OK) {
                WebUtils.discardEntityBytes(response);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    Collect.getInstance().getCookieStore().clear();
                }
                String webError = response.getStatusLine().getReasonPhrase()
                        + " (" + statusCode + ")";

                return new DocumentFetchResult(u.toString()
                        + " responded with: " + webError, statusCode);
            }

            if (entity == null) {
                String error = "No entity body returned from: " + u.toString();
                Timber.e(error);
                return new DocumentFetchResult(error, 0);
            }

            if (!entity.getContentType().getValue().toLowerCase(Locale.ENGLISH)
                    .contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML)) {
                WebUtils.discardEntityBytes(response);
                String error = "ContentType: "
                        + entity.getContentType().getValue()
                        + " returned from: "
                        + u.toString()
                        + " is not text/xml.  This is often caused a network proxy.  Do you need "
                        + "to login to your network?";
                Timber.e(error);
                return new DocumentFetchResult(error, 0);
            }
            // parse response
            Document doc = null;
            String hash;
            try {
                InputStream is = null;
                InputStreamReader isr = null;
                try {
                    byte[] bytes = IOUtils.toByteArray(entity.getContent());
                    is = new ByteArrayInputStream(bytes);
                    hash = FileUtils.getMd5Hash(new ByteArrayInputStream(bytes));
                    Header contentEncoding = entity.getContentEncoding();
                    if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(
                            WebUtils.GZIP_CONTENT_ENCODING)) {
                        is = new GZIPInputStream(is);
                    }
                    isr = new InputStreamReader(is, "UTF-8");
                    doc = new Document();
                    KXmlParser parser = new KXmlParser();
                    parser.setInput(isr);
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                            true);
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
                        } catch (Exception e) {
                            // no-op
                            Timber.e(e);
                        }
                        try {
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
                        + "while accessing " + u.toString();
                Timber.e(error);
                return new DocumentFetchResult(error, 0);
            }

            boolean isOR = false;
            Header[] fields = response
                    .getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
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
                    Timber.w("%s unrecognized version(s): %s", WebUtils.OPEN_ROSA_VERSION_HEADER, b.toString());
                }
            }
            return new DocumentFetchResult(doc, isOR, hash);
        } catch (Exception e) {
            String cause;
            Throwable c = e;
            while (c.getCause() != null) {
                c = c.getCause();
            }
            cause = c.toString();
            String error = "Error: " + cause + " while accessing "
                    + u.toString();

            Timber.w(error);
            return new DocumentFetchResult(error, 0);
        }
    }
}
