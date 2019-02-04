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
import org.odk.collect.android.utilities.ResponseMessageParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

    private static OkHttpClient httpClient;
    private static HttpCredentialsInterface httpCredentials;

    @NonNull
    @Override
    public HttpGetResult get(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        OkHttpClient client = createOkHttpClient(credentials);
        Request request = getRequest(uri);

        Response response = client.newCall(request).execute();
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
    public HttpHeadResult head(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        Map<String, String> responseHeaders = new HashMap<>();

        int statusCode;

        try {
            Timber.i("Issuing HEAD request to: %s", uri.toString());

            OkHttpClient client = createOkHttpClient(credentials);
            Request request = headRequest(uri);

            Response response = client.newCall(request).execute();
            statusCode = response.code();

            if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                Headers headers = response.headers();

                for (String headerName : headers.names()) {
                    responseHeaders.put(headerName, headers.get(headerName));
                }
            }

            discardEntityBytes(response);

        } catch (IOException | IllegalStateException e) {
            String errorMessage = "";

            if (e instanceof MalformedURLException) {
                errorMessage = "Malformed URL Exception";
            } else if (e instanceof IllegalStateException) {
                errorMessage = "Illegal State Exception";
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

    @NonNull
    @Override
    public ResponseMessageParser uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws IOException {
        ResponseMessageParser messageParser = null;

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
                            > 10000000L)) {
                        // the next file would exceed the 10MB threshold...
                        Timber.i("Extremely long post is being split into multiple posts");
                        multipartBuilder.addPart(MultipartBody.Part.createFormData("*isIncomplete*", "yes"));
                        ++fileIndex; // advance over the last attachment added...
                        break;
                    }
                }
            }

            MultipartBody multipartBody = multipartBuilder.build();

            try {
                OkHttpClient client = createOkHttpClient(credentials);
                Request request = postRequest(uri, multipartBody);
                Response response = client.newCall(request).execute();

                messageParser = new ResponseMessageParser(
                        response.toString(),
                        response.code(),
                        response.message());

                discardEntityBytes(response);

                if (response.code() != HttpURLConnection.HTTP_CREATED && response.code() != HttpURLConnection.HTTP_ACCEPTED) {
                    return messageParser;
                }
            } catch (IOException e) {
                Timber.e(e);
                String msg = e.getMessage();
                if (msg == null) {
                    msg = e.toString();
                }

                throw new IOException(msg);
            }
        }

        return messageParser;
    }

    private OkHttpClient createOkHttpClient(@Nullable HttpCredentialsInterface credentials) {
        OkHttpClient.Builder builder;

        if (httpClient != null) {
            if (sameCredentials(credentials)) {
                return httpClient;
            }
            builder = httpClient.newBuilder();
        } else {
            builder = new OkHttpClient.Builder();
        }

        addCredentials(builder, credentials);
        httpCredentials = credentials;

        httpClient = builder
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build();

        return httpClient;
    }

    private boolean sameCredentials(HttpCredentialsInterface credentials) {
        if (httpCredentials == null && credentials == null) {
            return true;
        } else if (httpCredentials == null || credentials == null) {
            return false;
        } else if (httpCredentials.equals(credentials)) {
            return true;
        }

        return false;
    }

    private void addCredentials(OkHttpClient.Builder builder, @Nullable HttpCredentialsInterface credentials) {
        if (credentials != null) {
            final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
            Credentials cred = new Credentials(credentials.getUsername(), credentials.getPassword());

            BasicAuthenticator basicAuthenticator = new BasicAuthenticator(cred);
            DigestAuthenticator digestAuthenticator = new DigestAuthenticator(cred);

            DispatchingAuthenticator authenticator = new DispatchingAuthenticator.Builder()
                    .with("digest", digestAuthenticator)
                    .with("basic", basicAuthenticator)
                    .build();

            builder.authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache));
        }
    }

    private Request getRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .get()
                .build();
    }

    private Request headRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .head()
                .build();
    }

    private Request postRequest(@NonNull URI uri, RequestBody body) throws MalformedURLException {
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
