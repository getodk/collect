package org.odk.collect.android.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpConnection implements OpenRosaHttpInterface {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int WRITE_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final int READ_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String CONTENT_ENCODING = "gzip,deflate";
    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";
    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    /**
     * Shared client object used for all HTTP requests. Credentials are set on a per-request basis.
     */
    private static OkHttpClient httpClient;

    /**
     * The credentials used for the last request. When a new request is made, this is used to see
     * whether the {@link #httpClient} credentials need to be changed.
     */
    private static HttpCredentialsInterface lastRequestCredentials;

    /**
     * The scheme used for the last request. When a new request is made, this is used to see
     * whether the {@link #httpClient} credentials need to be changed.
     */
    private static String lastRequestScheme = "";

    MultipartBody multipartBody;

    public OkHttpConnection() {
        if (httpClient == null) {
            initializeHttpClient();
        }
    }

    private synchronized void initializeHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        httpClient = builder
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build();
    }

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        setCredentialsIfNeeded(credentials, uri.getScheme());
        Request request = buildGetRequest(uri);

        Response response = httpClient.newCall(request).execute();
        int statusCode = response.code();

        if (statusCode != HttpURLConnection.HTTP_OK) {
            discardEntityBytes(response);
            String errMsg = Collect
                    .getInstance()
                    .getString(R.string.file_fetch_failed, uri.toString(), response.message(), String.valueOf(statusCode));

            Timber.e(errMsg);
            return new HttpGetResult(null, new HashMap<String, String>(), "", statusCode);
        }

        ResponseBody body = response.body();

        if (body == null) {
            throw new Exception("No entity body returned from: " + uri.toString());
        }

        if (contentType != null && contentType.length() > 0) {
            MediaType type = body.contentType();

            if (type != null && !type.toString().toLowerCase(Locale.ENGLISH).contains(contentType)) {
                discardEntityBytes(response);

                String error = "ContentType: " + type.toString() + " returned from: "
                        + uri.toString() + " is not " + contentType
                        + ".  This is often caused by a network proxy.  Do you need "
                        + "to login to your network?";

                throw new Exception(error);
            }
        }

        InputStream downloadStream = body.byteStream();

        String hash = "";

        if (HTTP_CONTENT_TYPE_TEXT_XML.equals(contentType)) {
            byte[] bytes = IOUtils.toByteArray(downloadStream);
            downloadStream = new ByteArrayInputStream(bytes);
            hash = FileUtils.getMd5Hash(new ByteArrayInputStream(bytes));
        }

        Map<String, String> responseHeaders = new HashMap<>();
        Headers headers = response.headers();

        for (int i = 0; i < headers.size(); i++) {
            responseHeaders.put(headers.name(i), headers.value(i));
        }

        return new HttpGetResult(downloadStream, responseHeaders, hash, statusCode);
    }

    @NonNull
    @Override
    public HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        setCredentialsIfNeeded(credentials, uri.getScheme());
        Request request = buildHeadRequest(uri);

        Timber.i("Issuing HEAD request to: %s", uri.toString());
        Response response = httpClient.newCall(request).execute();
        int statusCode = response.code();

        Map<String, String> responseHeaders = new HashMap<>();

        if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
            Headers headers = response.headers();

            for (String headerName : headers.names()) {
                responseHeaders.put(headerName, headers.get(headerName));
            }
        }

        discardEntityBytes(response);

        return new HttpHeadResult(statusCode, responseHeaders);
    }

    @NonNull
    @Override
    public HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        setCredentialsIfNeeded(credentials, uri.getScheme());
        HttpPostResult postResult;
        Request request = buildPostRequest(uri, multipartBody);
        Response response = httpClient.newCall(request).execute();

        postResult = new HttpPostResult(
                response.toString(),
                response.code(),
                response.message());

        discardEntityBytes(response);

        return postResult;
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws Exception {
        HttpPostResult postResult = null;

        boolean first = true;
        int fileIndex = 0;
        int lastFileIndex;
        while (fileIndex < fileList.size() || first) {
            lastFileIndex = fileIndex;
            first = false;

            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            long byteCount = 0L;

            RequestBody requestBody = RequestBody.create(MediaType.parse(HTTP_CONTENT_TYPE_TEXT_XML), submissionFile);

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(MultipartBody.Part.createFormData("xml_submission_file", submissionFile.getName(), requestBody));

            Timber.i("added xml_submission_file: %s", submissionFile.getName());
            byteCount += submissionFile.length();

            for (; fileIndex < fileList.size(); fileIndex++) {
                File file = fileList.get(fileIndex);

                String mime = mimeTypeMap.getMimeTypeFromExtension(FileUtils.getFileExtension(file.getName()));

                RequestBody fileRequestBody = RequestBody.create(MediaType.parse(mime), file);

                multipartBuilder.addPart(MultipartBody.Part.create(fileRequestBody));

                byteCount += file.length();
                Timber.i("added file of type '%s' %s", mime, file.getName());

                // we've added at least one attachment to the request...
                if (fileIndex + 1 < fileList.size()) {
                    if ((fileIndex - lastFileIndex + 1 > 100) || (byteCount + fileList.get(fileIndex + 1).length()
                            > contentLength)) {
                        // the next file would exceed the 10MB threshold...
                        Timber.i("Extremely long post is being split into multiple posts");
                        multipartBuilder.addPart(MultipartBody.Part.createFormData("*isIncomplete*", "yes"));
                        ++fileIndex; // advance over the last attachment added...
                        break;
                    }
                }
            }

            multipartBody = multipartBuilder.build();
            postResult = executePostRequest(uri, credentials);
            multipartBody = null;

            if (postResult.getResponseCode() != HttpURLConnection.HTTP_CREATED &&
                    postResult.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                return postResult;
            }

        }

        return postResult;
    }

    /**
     * If the provided credentials are non-null, sets the {@link #httpClient} to authenticate using
     * the provided credential and sets the {@link #lastRequestCredentials}
     *
     * If authentication is needed, always configure digest auth. If SSL is enabled, also configure
     * basic auth.
     *
     */
    private void setCredentialsIfNeeded(@Nullable HttpCredentialsInterface credentials, String scheme) {
        if (credentials == null || (credentials.equals(lastRequestCredentials) && scheme.equals(lastRequestScheme))) {
            return;
        }

        final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
        Credentials cred = new Credentials(credentials.getUsername(), credentials.getPassword());

        DispatchingAuthenticator.Builder daBuilder = new DispatchingAuthenticator.Builder();

        if (scheme.equalsIgnoreCase("https")) {
            daBuilder.with("basic", new BasicAuthenticator(cred));
        }

        daBuilder.with("digest", new DigestAuthenticator(cred));

        DispatchingAuthenticator authenticator = daBuilder.build();

        initializeHttpClient(); // Need to initalise the http client again to get rid of the cached credentials

        httpClient = httpClient.newBuilder().authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache)).build();

        lastRequestCredentials = credentials;
        lastRequestScheme = scheme;
    }

    private Request buildGetRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .get()
                .build();
    }

    private Request buildHeadRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .head()
                .build();
    }

    private Request buildPostRequest(@NonNull URI uri, RequestBody body) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .post(body)
                .build();
    }

    private String getUserAgentString() {
        return String.format("%s %s/%s",
                System.getProperty("http.agent"),
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME);
    }

    private String getHeaderDate() {
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        g.setTime(new Date());
        return DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString();
    }

    /**
     * Utility to ensure that the entity stream of a response is drained of
     * bytes.
     * Apparently some servers require that we manually read all data from the
     * stream to allow its re-use.  Please add more details or bug ID here if
     * you know them.
     */
    private void discardEntityBytes(Response response) {
        ResponseBody body = response.body();
        if (body != null) {
            try (InputStream is = body.byteStream()) {
                while (is.read() != -1) {
                    // loop until all bytes read
                }
            } catch (Exception e) {
                Timber.i(e);
            }
        }
    }

}
