package org.odk.collect.android.utilities;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
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
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.config.AuthSchemes;
import org.opendatakit.httpclientandroidlib.client.config.CookieSpecs;
import org.opendatakit.httpclientandroidlib.client.config.RequestConfig;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.client.protocol.HttpClientContext;
import org.opendatakit.httpclientandroidlib.config.SocketConfig;
import org.opendatakit.httpclientandroidlib.impl.auth.BasicScheme;
import org.opendatakit.httpclientandroidlib.impl.client.BasicAuthCache;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;
import org.opendatakit.httpclientandroidlib.impl.client.HttpClientBuilder;
import org.opendatakit.httpclientandroidlib.protocol.BasicHttpContext;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import timber.log.Timber;

public class ClientHttpConnection implements CollectHttpConnection {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String GZIP_CONTENT_ENCODING = "gzip";


    private CredentialsProvider credentialsProvider;
    private CookieStore cookieStore;

    ClientHttpConnection() {
        credentialsProvider = new WebUtils.AgingCredentialsProvider(7 * 60 * 1000);
        cookieStore = new BasicCookieStore();
    }

    @Override
    public InputStream getInputStream(@NonNull URI uri, String contentType, final int connectionTimeout, Map<String,String> responseHeaders) throws Exception {
        HttpContext localContext = getHttpContext();
        HttpClient httpclient = createHttpClient(connectionTimeout);

        // if https then enable preemptive basic auth...
        if (uri.getScheme().equals("https")) {
            enablePreemptiveBasicAuth(localContext, uri.getHost());
        }

        // set up request...
        HttpGet req = createOpenRosaHttpGet(uri);
        req.addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING);

        HttpResponse response;

        response = httpclient.execute(req, localContext);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            discardEntityBytes(response);
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                // clear the cookies -- should not be necessary?
                getCookieStore().clear();
            }
            String errMsg =
                    Collect.getInstance().getString(R.string.file_fetch_failed, uri.toString(),
                            response.getStatusLine().getReasonPhrase(), String.valueOf(statusCode));
            Timber.e(errMsg);
            throw new Exception(errMsg);
        }

        HttpEntity entity = response.getEntity();

        if (entity == null) {
            throw new Exception("No entity body returned from: " + uri.toString());
        }

        if (contentType != null && contentType.length()>0) {
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
        Header contentEncoding = entity.getContentEncoding();
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(GZIP_CONTENT_ENCODING)) {
            downloadStream = new GZIPInputStream(downloadStream);
        }

        if (responseHeaders != null) {
            Header[] fields = response.getAllHeaders();

            if (fields != null && fields.length >= 1) {
                for (Header h : fields) {
                    responseHeaders.put(h.getName(), h.getValue());
                }
            }
        }

        return downloadStream;
    }

    @Override
    public void clearCookieStore() {
        getCookieStore().clear();
    }

    public synchronized HttpContext getHttpContext() {

        // context holds authentication state machine, so it cannot be
        // shared across independent activities.
        HttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(HttpClientContext.COOKIE_STORE, getCookieStore());
        localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, getCredentialsProvider());

        return localContext;
    }

    /**
     * Create an httpClient with connection timeouts and other parameters set.
     * Save and reuse the connection manager across invocations (this is what
     * requires synchronized access).
     *
     * @return HttpClient properly configured.
     */
    public synchronized HttpClient createHttpClient(int timeout) {
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

    private void enablePreemptiveBasicAuth(
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

    private List<AuthScope> buildAuthScopes(String host) {
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

    private CookieStore getCookieStore() {
        return cookieStore;
    }

    private CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
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
    @Override
    public void addCredentials(String username, String password, String host) {
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
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        g.setTime(new Date());
        req.setHeader(DATE_HEADER,
                DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString());
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
}
