/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
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
import org.opendatakit.httpclientandroidlib.client.CookieStore;
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
import org.opendatakit.httpclientandroidlib.protocol.BasicHttpContext;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.opendatakit.httpclientandroidlib.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
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

public class HttpClientConnection implements OpenRosaHttpInterface {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String GZIP_CONTENT_ENCODING = "gzip";

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int UPLOAD_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    // Retain authentication and cookies between requests. Gets mutated on each call to
    // HttpClient.execute).
    private HttpContext httpContext;

    public HttpClientConnection() {
        httpContext = new BasicHttpContext();

        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
        httpContext.setAttribute(HttpClientContext.CREDS_PROVIDER, new AgingCredentialsProvider(7 * 60 * 1000));
    }

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

    @Override
    public @NonNull
    HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable final String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        addCredentialsForHost(uri, credentials);
        clearCookieStore();

        HttpClient httpclient = createHttpClient(CONNECTION_TIMEOUT);

        // if https then enable preemptive basic auth...
        if (uri.getScheme().equals("https")) {
            enablePreemptiveBasicAuth(uri.getHost());
        }

        // set up request...
        HttpGet req = createOpenRosaHttpGet(uri);
        req.addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING);

        HttpResponse response;

        response = httpclient.execute(req, httpContext);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            discardEntityBytes(response);
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                // clear the cookies -- should not be necessary?
                clearCookieStore();
            }
            String errMsg =
                    Collect.getInstance().getString(R.string.file_fetch_failed, uri.toString(),
                            response.getStatusLine().getReasonPhrase(), String.valueOf(statusCode));
            Timber.e(errMsg);

            return new HttpGetResult(null, new HashMap<String, String>(), "", statusCode);
        }

        HttpEntity entity = response.getEntity();

        if (entity == null) {
            throw new Exception("No entity body returned from: " + uri.toString());
        }

        if (contentType != null && contentType.length() > 0) {
            if (!entity.getContentType().getValue().toLowerCase(Locale.ENGLISH).contains(contentType)) {
                discardEntityBytes(response);
                String error = "ContentType: "
                        + entity.getContentType().getValue()
                        + " returned from: "
                        + uri.toString()
                        + " is not " + contentType + ".  This is often caused by a network proxy.  Do you need "
                        + "to login to your network?";

                throw new Exception(error);
            }
        }

        InputStream downloadStream = entity.getContent();

        String hash = "";

        if (HTTP_CONTENT_TYPE_TEXT_XML.equals(contentType)) {
            byte[] bytes = IOUtils.toByteArray(downloadStream);
            downloadStream = new ByteArrayInputStream(bytes);
            hash = FileUtils.getMd5Hash(new ByteArrayInputStream(bytes));
        }

        Header contentEncoding = entity.getContentEncoding();
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(GZIP_CONTENT_ENCODING)) {
            downloadStream = new GZIPInputStream(downloadStream);
        }

        Map<String, String> responseHeaders = new HashMap<>();

        Header[] fields = response.getAllHeaders();

        if (fields != null && fields.length >= 1) {
            for (Header h : fields) {
                responseHeaders.put(h.getName(), h.getValue());
            }
        }

        return new HttpGetResult(downloadStream, responseHeaders, hash, statusCode);
    }

    @Override
    public @NonNull HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        addCredentialsForHost(uri, credentials);
        clearCookieStore();

        HttpClient httpclient = createHttpClient(CONNECTION_TIMEOUT);
        HttpHead httpHead = createOpenRosaHttpHead(uri);
        Map<String, String> responseHeaders = new HashMap<>();

        // if https then enable preemptive basic auth...
        if (uri.getScheme() != null && uri.getScheme().equals("https")) {
            enablePreemptiveBasicAuth(uri.getHost());
        }

        final HttpResponse response;
        int statusCode;

        try {
            Timber.i("Issuing HEAD request to: %s", uri.toString());

            response = httpclient.execute(httpHead, httpContext);
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                clearCookieStore();
            } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
                for (Header head : response.getAllHeaders()) {
                    responseHeaders.put(head.getName(), head.getValue());
                }
            }

            discardEntityBytes(response);

        } catch (ClientProtocolException | ConnectTimeoutException | UnknownHostException | SocketTimeoutException | NoHttpResponseException | SocketException e) {
            String errorMessage;

            if (e instanceof ClientProtocolException) {
                errorMessage = "Client Protocol Exception";
            } else if (e instanceof ConnectTimeoutException) {
                errorMessage = "Connection Timeout";
            } else if (e instanceof UnknownHostException) {
                errorMessage = e.toString() + " :: Network Connection Failed";
            } else if (e instanceof SocketTimeoutException) {
                errorMessage = "Connection Timeout";
            } else {
                errorMessage = "Network Connection Refused";
            }

            throw new Exception(errorMessage);

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }

            throw new Exception("Generic Exception: " + msg);
        }

        return new HttpHeadResult(statusCode, responseHeaders);
    }

    @Override
    public @NonNull HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList,
                                                        @NonNull File submissionFile,
                                                        @NonNull URI uri,
                                                        @Nullable HttpCredentialsInterface credentials,
                                                        @NonNull long contentLength) throws IOException {
        addCredentialsForHost(uri, credentials);
        clearCookieStore();

        HttpClient httpclient = createHttpClient(UPLOAD_CONNECTION_TIMEOUT);

        // if https then enable preemptive basic auth...
        if (uri.getScheme().equals("https")) {
            enablePreemptiveBasicAuth(uri.getHost());
        }

        HttpPostResult postResult = null;

        boolean first = true;
        int fileIndex = 0;
        int lastFileIndex;
        while (fileIndex < fileList.size() || first) {
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

            for (; fileIndex < fileList.size(); fileIndex++) {
                File file = fileList.get(fileIndex);

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
                if (fileIndex + 1 < fileList.size()) {
                    if ((fileIndex - lastFileIndex + 1 > 100) || (byteCount + fileList.get(fileIndex + 1).length()
                            > contentLength)) {
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

            HttpPost httppost = createOpenRosaHttpPost(uri);
            httppost.setEntity(builder.build());

            // prepare response and return uploaded
            HttpResponse response;

            try {
                Timber.i("Issuing POST request to: %s", uri.toString());
                response = httpclient.execute(httppost, httpContext);
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity httpEntity = response.getEntity();
                Timber.i("Response code:%d", responseCode);

                postResult = new HttpPostResult(EntityUtils.toString(httpEntity), responseCode, response.getStatusLine().getReasonPhrase());

                discardEntityBytes(response);

                if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                    clearCookieStore();
                }

                if (responseCode != HttpStatus.SC_CREATED && responseCode != HttpStatus.SC_ACCEPTED) {
                    return postResult;
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

                throw new IOException(msg);
            }
        }

        return postResult;
    }

    /**
     * HttpPostResult - This is just stubbed out for now, implemented when we move to OkHttpConnection
     * @param uri of which to post
     * @param credentials to use on this post request
     * @return null
     * @throws Exception not used
     */
    public HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        return new HttpPostResult("", 0, "");
    }

    private void addCredentialsForHost(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) {
        if (credentials != null) {
            addCredentials(credentials.getUsername(), credentials.getPassword(), uri.getHost());
        }
    }

    /**
     * Create an httpClient with connection timeouts and other parameters set.
     * Save and reuse the connection manager across invocations (this is what
     * requires synchronized access).
     *
     * @return HttpClient properly configured.
     */
    private synchronized HttpClient createHttpClient(int timeout) {
        // configure connection
        SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT).setSoTimeout(
                2 * timeout)
                .build();

        // if possible, bias toward digest auth (may not be in 4.0 beta 2)
        List<String> targetPreferredAuthSchemes = new ArrayList<>();
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

    private void enablePreemptiveBasicAuth(String host) {
        AuthCache ac = (AuthCache) httpContext.getAttribute(HttpClientContext.AUTH_CACHE);
        HttpHost h = new HttpHost(host);
        if (ac == null) {
            ac = new BasicAuthCache();
            httpContext.setAttribute(HttpClientContext.AUTH_CACHE, ac);
        }
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope authScope : asList) {
            if (authScope.getScheme().equalsIgnoreCase(AuthSchemes.BASIC)) {
                ac.put(h, new BasicScheme());
            }
        }
    }

    private List<AuthScope> buildAuthScopes(String host) {
        List<AuthScope> asList = new ArrayList<>();

        // allow digest auth on any port...
        asList.add(new AuthScope(host, -1, null, AuthSchemes.DIGEST));
        // and allow basic auth on the standard TLS/SSL ports...
        asList.add(new AuthScope(host, 443, null, AuthSchemes.BASIC));
        asList.add(new AuthScope(host, 8443, null, AuthSchemes.BASIC));

        return asList;
    }

    private void clearCookieStore() {
        ((CookieStore) httpContext.getAttribute(HttpClientContext.COOKIE_STORE)).clear();
    }

    private CredentialsProvider getCredentialsProvider() {
        return (CredentialsProvider) httpContext.getAttribute(HttpClientContext.CREDS_PROVIDER);
    }

    public void clearHostCredentials(String host) {
        CredentialsProvider credsProvider = getCredentialsProvider();
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

    public synchronized void addCredentials(String username, String password, String host) {
        // to ensure that this is the only authentication available for this
        // host...
        clearHostCredentials(host);
        if (username != null && username.trim().length() != 0) {
            Timber.i("adding credential for host: %s username:%s", host, username);
            Credentials c = new UsernamePasswordCredentials(username, password);
            addCredentials(c, host);
        }
    }

    private void addCredentials(Credentials c, String host) {
        CredentialsProvider credsProvider = getCredentialsProvider();
        List<AuthScope> asList = buildAuthScopes(host);
        for (AuthScope a : asList) {
            credsProvider.setCredentials(a, c);
        }
    }

    private static HttpGet createOpenRosaHttpGet(URI uri) {
        HttpGet req = new HttpGet();
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        req.setURI(uri);
        return req;
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
        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gregorianCalendar.setTime(new Date());
        req.setHeader(DATE_HEADER,
                DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", gregorianCalendar).toString());
    }

    /**
     * Utility to ensure that the entity stream of a response is drained of
     * bytes.
     * Apparently some servers require that we manually read all data from the
     * stream to allow its re-use.  Please add more details or bug ID here if
     * you know them.
     */
    private void discardEntityBytes(HttpResponse response) {
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

    private HttpHead createOpenRosaHttpHead(URI uri) {
        HttpHead req = new HttpHead(uri);
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        return req;
    }

    private HttpPost createOpenRosaHttpPost(URI uri) {
        HttpPost req = new HttpPost(uri);
        setCollectHeaders(req);
        setOpenRosaHeaders(req);
        return req;
    }

    public static class AgingCredentialsProvider implements CredentialsProvider {

        private final ConcurrentHashMap<AuthScope, Credentials> credMap;
        private final long expiryInterval;

        private long nextClearTimestamp;

        private AgingCredentialsProvider(int expiryInterval) {
            super();
            this.credMap = new ConcurrentHashMap<>();
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
}
