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

import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.opendatakit.httpclientandroidlib.Header;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpHost;
import org.opendatakit.httpclientandroidlib.HttpRequest;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.NoHttpResponseException;
import org.opendatakit.httpclientandroidlib.auth.AuthScope;
import org.opendatakit.httpclientandroidlib.auth.Credentials;
import org.opendatakit.httpclientandroidlib.auth.UsernamePasswordCredentials;
import org.opendatakit.httpclientandroidlib.client.AuthCache;
import org.opendatakit.httpclientandroidlib.client.ClientProtocolException;
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
import org.opendatakit.httpclientandroidlib.conn.ConnectTimeoutException;
import org.opendatakit.httpclientandroidlib.conn.HttpHostConnectException;
import org.opendatakit.httpclientandroidlib.entity.ContentType;
import org.opendatakit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import org.opendatakit.httpclientandroidlib.entity.mime.content.FileBody;
import org.opendatakit.httpclientandroidlib.entity.mime.content.StringBody;
import org.opendatakit.httpclientandroidlib.impl.auth.BasicScheme;
import org.opendatakit.httpclientandroidlib.impl.client.BasicAuthCache;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;
import org.opendatakit.httpclientandroidlib.impl.client.HttpClientBuilder;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.protocol.BasicHttpContext;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.opendatakit.httpclientandroidlib.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String GZIP_CONTENT_ENCODING = "gzip";

    private static final String fail = "Error: ";

    private static WebUtils instance;

    private CollectHttpConnection httpConnection;

    private CredentialsProvider credentialsProvider;
    private CookieStore cookieStore;

    private WebUtils() {
        credentialsProvider = new WebUtils.AgingCredentialsProvider(7 * 60 * 1000);
        cookieStore = new BasicCookieStore();
        httpConnection = new ClientHttpConnection();
    }

    private static synchronized WebUtils getInstance() {
        if (instance == null) {
            instance = new WebUtils();
        }
        return instance;
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
        CredentialsProvider credsProvider = WebUtils.getInstance()
                .getCredentialsProvider();
        Timber.i("clearHostCredentials: %s", host);
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, null);
        }
    }

    public static void clearCookieStore() {
        WebUtils.getInstance().getCookieStore().clear();
    }

    /**
     * Construct and return a session context with shared cookieStore and credsProvider so a user
     * does not have to re-enter login information.
     */
    public static synchronized HttpContext getHttpContext() {

        // context holds authentication state machine, so it cannot be
        // shared across independent activities.
        HttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(HttpClientContext.COOKIE_STORE, WebUtils.getInstance().getCookieStore());
        localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, WebUtils.getInstance().getCredentialsProvider());

        return localContext;
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
        CredentialsProvider credsProvider = WebUtils.getInstance()
                .getCredentialsProvider();
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, c);
        }
    }

    private static void enablePreemptiveBasicAuth(
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

    private static HttpHead createOpenRosaHttpHead(URI uri) {
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

    private static HttpPost createOpenRosaHttpPost(Uri u) {
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
    private static void discardEntityBytes(HttpResponse response) {
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
    public static DocumentFetchResult getXmlDocument(String urlString) {

        // parse response
        Document doc = null;

        InputStreamResult inputStreamResult = null;

        try {
            inputStreamResult = getDownloadInputStream(urlString, WebUtils.HTTP_CONTENT_TYPE_TEXT_XML);
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
                    + "while accessing " + urlString;
            Timber.e(error);
            return new DocumentFetchResult(error, 0);
        }

        return new DocumentFetchResult(doc, inputStreamResult.isOpenRosaResponse());
    }


    /**
     * Instantiates a file InputStream from a URI
     *
     * @param downloadUrl
     * @return InputStream
     * @throws Exception
     */
    public static InputStreamResult getDownloadInputStream(@NonNull String downloadUrl, final String contentType) throws Exception {
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

        Map<String, String> responseHeaders = new HashMap<String, String>();
        CollectHttpConnection httpConnection = WebUtils.getInstance().getHttpConnection();
        InputStream downloadStream = httpConnection.getInputStream(uri, contentType, WebUtils.CONNECTION_TIMEOUT, responseHeaders);

        boolean openRosaResponse = false;

        if (!responseHeaders.isEmpty()) {

            boolean versionMatch = false;
            boolean first = true;

            StringBuilder appendedVersions = new StringBuilder();

            for (String key : responseHeaders.keySet()) {
                if (key.equals(WebUtils.OPEN_ROSA_VERSION_HEADER)) {
                    openRosaResponse = true;
                    if (WebUtils.OPEN_ROSA_VERSION.equals(responseHeaders.get(key))) {
                        versionMatch = true;
                        break;
                    }
                    if (!first) {
                        appendedVersions.append("; ");
                    }
                    first = false;
                    appendedVersions.append(responseHeaders.get(key));
                }
            }
            if (!versionMatch) {
                Timber.w("%s unrecognized version(s): %s", WebUtils.OPEN_ROSA_VERSION_HEADER, appendedVersions.toString());
            }
        }

        return new InputStreamResult(downloadStream,openRosaResponse);
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public CollectHttpConnection getHttpConnection() {
        return httpConnection;
    }

    /**
     *
     */
    private enum ContentTypeMapping {
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
            String extension = FileUtils.getFileExtension(fileName);

            for (ContentTypeMapping m : values()) {
                if (m.extension.equals(extension)) {
                    return m.contentType;
                }
            }

            return null;
        }
    }


    private static List<File> getFilesInParentDirectory(File instanceFile, File submissionFile, Boolean openRosaServer) {
        List<File> files = new ArrayList<File>();

        // find all files in parent directory
        File[] allFiles = instanceFile.getParentFile().listFiles();
        if (allFiles == null) { return null; }

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


    /**
     * Uploads a file to a url
     *
     * @param urlString - The Destination URL
     * @param id - Form ID
     * @param instanceFilePath - path + filename to upload
     * @param toUpdate - Content Provider URI to update
     * @param uriRemap - Map of uri's that are to be re-mapped to a submission URI
     * @param outcome - An object to hold the results of the file upload
     * @return false if credentials are required and we should terminate immediately.
     */
    public static boolean uploadFile(String urlString, String id, String instanceFilePath,
                              Uri toUpdate,
                              Map<Uri, Uri> uriRemap,
                              Outcome outcome) {

        ContentValues contentValues = new ContentValues();
        Uri submissionUri = Uri.parse(urlString);

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = getHttpContext();
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        ResponseMessageParser messageParser = null;
        boolean openRosaServer = false;
        if (uriRemap.containsKey(submissionUri)) {
            // we already issued a head request and got a response,
            // so we know the proper URL to send the submission to
            // and the proper scheme. We also know that it was an
            // OpenRosa compliant server.
            openRosaServer = true;
            submissionUri = uriRemap.get(submissionUri);

            // if https then enable preemptive basic auth...
            if (submissionUri.getScheme().equals("https")) {
                WebUtils.enablePreemptiveBasicAuth(localContext, submissionUri.getHost());
            }

            Timber.i("Using Uri remap for submission %s. Now: %s", id, submissionUri.toString());
        } else {
            if (submissionUri.getHost() == null) {
                Timber.i("Host name may not be null");
                outcome.messagesByInstanceId.put(id, fail + "Host name may not be null");
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
                return true;
            }

            // if https then enable preemptive basic auth...
            if (submissionUri.getScheme() != null && submissionUri.getScheme().equals("https")) {
                WebUtils.enablePreemptiveBasicAuth(localContext, submissionUri.getHost());
            }

            URI uri;
            try {
                uri = URI.create(submissionUri.toString());
            } catch (IllegalArgumentException e) {
                Timber.i(e);
                outcome.messagesByInstanceId.put(id, Collect.getInstance().getString(R.string.url_error));
                return false;
            }

            // Issue a head request to confirm the server is an OpenRosa server and see if auth
            // is required
            // http://docs.opendatakit.org/openrosa-form-submission/#extended-transmission-considerations
            HttpHead httpHead = WebUtils.createOpenRosaHttpHead(uri);

            // prepare response
            final HttpResponse response;
            try {
                Timber.i("Issuing HEAD request for %s to: %s", id, submissionUri.toString());

                response = httpclient.execute(httpHead, localContext);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    WebUtils.getInstance().getCookieStore().clear();

                    WebUtils.discardEntityBytes(response);
                    // we need authentication, so stop and return what we've
                    // done so far.
                    outcome.authRequestingServer = submissionUri;
                    return false;
                } else if (statusCode == 204) {
                    Header[] locations = response.getHeaders("Location");
                    WebUtils.discardEntityBytes(response);
                    if (locations != null && locations.length == 1) {
                        try {
                            Uri newURI = Uri.parse(
                                    URLDecoder.decode(locations[0].getValue(), "utf-8"));
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
                    // may be a server that does not handle
                    WebUtils.discardEntityBytes(response);

                    Timber.w("Status code on Head request: %d", statusCode);
                    if (statusCode >= HttpStatus.SC_OK
                            && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
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
            } catch (ClientProtocolException | ConnectTimeoutException | UnknownHostException | SocketTimeoutException | NoHttpResponseException | SocketException e) {
                if (e instanceof ClientProtocolException) {
                    outcome.messagesByInstanceId.put(id, fail + "Client Protocol Exception");
                    Timber.i(e, "Client Protocol Exception");
                } else if (e instanceof ConnectTimeoutException) {
                    outcome.messagesByInstanceId.put(id, fail + "Connection Timeout");
                    Timber.i(e, "Connection Timeout");
                } else if (e instanceof UnknownHostException) {
                    outcome.messagesByInstanceId.put(id, fail + e.toString() + " :: Network Connection Failed");
                    Timber.i(e, "Network Connection Failed");
                } else if (e instanceof SocketTimeoutException) {
                    outcome.messagesByInstanceId.put(id, fail + "Connection Timeout");
                    Timber.i(e, "Connection timeout");
                } else {
                    outcome.messagesByInstanceId.put(id, fail + "Network Connection Refused");
                    Timber.i(e, "Network Connection Refused");
                }
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
                return true;
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }
                outcome.messagesByInstanceId.put(id, fail + "Generic Exception: " + msg);
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

        boolean first = true;
        int fileIndex = 0;
        int lastFileIndex;
        while (fileIndex < files.size() || first) {
            lastFileIndex = fileIndex;
            first = false;

            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            long byteCount = 0L;

            // mime post
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // add the submission file first...
            FileBody fb = new FileBody(submissionFile, ContentType.TEXT_XML);
            builder.addPart("xml_submission_file", fb);
            Timber.i("added xml_submission_file: %s", submissionFile.getName());
            byteCount += submissionFile.length();

            for (; fileIndex < files.size(); fileIndex++) {
                File file = files.get(fileIndex);

                // we will be processing every one of these, so
                // we only need to deal with the content type determination...
                ContentType contentType = ContentTypeMapping.of(file.getName());
                if (contentType == null) {
                    String mime = mimeTypeMap.getMimeTypeFromExtension(FileUtils.getFileExtension(file.getName()));
                    if (mime != null) {
                        contentType = ContentType.create(mime);
                    } else {
                        Timber.w("No specific MIME type found for file: %s", file.getName());
                        contentType = ContentType.APPLICATION_OCTET_STREAM;
                    }
                }
                fb = new FileBody(file, contentType);
                builder.addPart(file.getName(), fb);
                byteCount += file.length();
                Timber.i("added file of type '%s' %s", contentType, file.getName());

                // we've added at least one attachment to the request...
                if (fileIndex + 1 < files.size()) {
                    if ((fileIndex - lastFileIndex + 1 > 100) || (byteCount + files.get(fileIndex + 1).length()
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
                        ++fileIndex; // advance over the last attachment added...
                        break;
                    }
                }
            }

            HttpPost httppost = WebUtils.createOpenRosaHttpPost(submissionUri);
            httppost.setEntity(builder.build());

            // prepare response and return uploaded
            HttpResponse response;

            try {
                Timber.i("Issuing POST request for %s to: %s", id, submissionUri.toString());
                response = httpclient.execute(httppost, localContext);
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity httpEntity = response.getEntity();

                messageParser = new ResponseMessageParser(EntityUtils.toString(httpEntity));
                WebUtils.discardEntityBytes(response);
                Timber.i("Response code:%d", responseCode);
                // verify that the response was a 201 or 202.
                // If it wasn't, the submission has failed.
                if (responseCode != HttpStatus.SC_CREATED
                        && responseCode != HttpStatus.SC_ACCEPTED) {
                    if (responseCode == HttpStatus.SC_OK) {
                        outcome.messagesByInstanceId.put(id, fail + "Network login failure? Again?");
                    } else if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                        // clear the cookies -- should not be necessary?
                        WebUtils.getInstance().getCookieStore().clear();
                        outcome.messagesByInstanceId.put(id, fail + response.getStatusLine().getReasonPhrase()
                                + " (" + responseCode + ") at " + urlString);
                    } else {
                        // If response from server is valid use that else use default messaging
                        if (messageParser.isValid()) {
                            outcome.messagesByInstanceId.put(id, fail + messageParser.getMessageResponse());
                        } else {
                            outcome.messagesByInstanceId.put(id, fail + response.getStatusLine().getReasonPhrase()
                                    + " (" + responseCode + ") at " + urlString);
                        }

                    }
                    contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS,
                            InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                    Collect.getInstance().getContentResolver()
                            .update(toUpdate, contentValues, null, null);
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
                outcome.messagesByInstanceId.put(id, fail + "Generic Exception: " + msg);
                contentValues.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                Collect.getInstance().getContentResolver().update(toUpdate, contentValues, null, null);
                return true;
            }
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

    public static String getPlainTextMimeType() {
        return ContentType.TEXT_PLAIN.getMimeType();
    }

    public static class Outcome {
        public Uri authRequestingServer = null;
        public boolean invalidOAuth;
        public HashMap<String, String> messagesByInstanceId = new HashMap<>();
    }


    public static class AgingCredentialsProvider implements CredentialsProvider {

        private final ConcurrentHashMap<AuthScope, Credentials> credMap;
        private final long expiryInterval;

        private long nextClearTimestamp;

        /**
         * Default constructor.
         */
        public AgingCredentialsProvider(int expiryInterval) {
            super();
            this.credMap = new ConcurrentHashMap<AuthScope, Credentials>();
            this.expiryInterval = expiryInterval;
            nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
        }

        public void setCredentials(
                final AuthScope authscope,
                final Credentials credentials) {
            if (authscope == null) {
                throw new IllegalArgumentException("Authentication scope may not be null");
            }
            if (nextClearTimestamp < System.currentTimeMillis()) {
                clear();
            }
            nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
            if (credentials == null) {
                credMap.remove(authscope);
            } else {
                credMap.put(authscope, credentials);
            }
        }

        /**
         * Find matching {@link Credentials credentials} for the given authentication scope.
         *
         * @param map       the credentials hash map
         * @param authscope the {@link AuthScope authentication scope}
         * @return the credentials
         */
        private Credentials matchCredentials(
                final Map<AuthScope, Credentials> map,
                final AuthScope authscope) {
            // see if we get a direct hit
            Credentials creds = map.get(authscope);
            if (creds == null) {
                // Nope.
                // Do a full scan
                int bestMatchFactor = -1;
                AuthScope bestMatch = null;
                for (AuthScope current : map.keySet()) {
                    int factor = authscope.match(current);
                    if (factor > bestMatchFactor) {
                        bestMatchFactor = factor;
                        bestMatch = current;
                    }
                }
                if (bestMatch != null) {
                    creds = map.get(bestMatch);
                }
            }
            return creds;
        }

        public Credentials getCredentials(final AuthScope authscope) {
            if (authscope == null) {
                throw new IllegalArgumentException("Authentication scope may not be null");
            }
            if (nextClearTimestamp < System.currentTimeMillis()) {
                clear();
            }
            nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
            return matchCredentials(this.credMap, authscope);
        }

        public void clear() {
            this.credMap.clear();
        }

        @Override
        public String toString() {
            return credMap.toString();
        }

    }

    public static final class InputStreamResult {
        private InputStream inputStream;
        private boolean openRosaResponse;

        InputStreamResult(InputStream is, boolean isOpenRosa) {
            inputStream = is;
            openRosaResponse = isOpenRosa;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public boolean isOpenRosaResponse() {
            return openRosaResponse;
        }
    }

}