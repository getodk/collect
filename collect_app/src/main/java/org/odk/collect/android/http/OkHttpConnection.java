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
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpConnection implements HttpInterface {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int WRITE_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final int READ_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String GZIP_CONTENT_ENCODING = "gzip";
    private static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";
    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";


    @NonNull
    @Override
    public HttpInputStreamResult getHttpInputStream(@NonNull URI uri, @Nullable String contentType) throws Exception {
        OkHttpClient client = createOkHttpClient();
        Request request = getRequest(uri);
        Response response = client.newCall(request).execute();
        int statusCode = response.code();

        if (statusCode != HttpURLConnection.HTTP_OK) {
            discardEntityBytes(response);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // clear the cookies -- should not be necessary?
//TODO:                getCookieStore().clear();
            }
            String errMsg = Collect
                    .getInstance()
                    .getString(R.string.file_fetch_failed, uri.toString(), response.message(), String.valueOf(statusCode));

            Timber.e(errMsg);
            throw new Exception(errMsg);
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

        if (headers.size() > 0) {
            for (int i=0; i<headers.size(); i++) {
                responseHeaders.put(headers.name(i), headers.value(i));
            }
        }

        return new HttpInputStreamResult(downloadStream, responseHeaders, hash);
    }

    @Override
    public int httpHeadRequest(@NonNull URI uri, @NonNull Map<String, String> responseHeaders) throws Exception {
        int statusCode;

        try {
            Timber.i("Issuing HEAD request to: %s", uri.toString());

            OkHttpClient client = createOkHttpClient();
            Request request = headRequest(uri);
            Response response = client.newCall(request).execute();
            statusCode = response.code();

            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
//TODO:                getCookieStore().clear();

            } else if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                for (int i = 0; i < response.headers().size(); i++) {
                    responseHeaders.put(response.headers().name(i), response.headers().value(i));
                }
            }

            discardEntityBytes(response);

        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }

            throw new Exception("Generic Exception: " + msg);
        }

        return statusCode;
    }

    @Override
    public ResponseMessageParser uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri)  throws IOException {
        ResponseMessageParser messageParser = null;


        boolean first = true;
        int fileIndex = 0;
        int lastFileIndex;
        while (fileIndex < fileList.size() || first) {
            lastFileIndex = fileIndex;
            first = false;

            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            long byteCount = 0L;

            MultipartBody multipartBody = (MultipartBody) MultipartBody.create(MediaType.parse("HTTP_CONTENT_TYPE_TEXT_XML"), submissionFile);

            Timber.i("added xml_submission_file: %s", submissionFile.getName());
            byteCount += submissionFile.length();

            for (; fileIndex < fileList.size(); fileIndex++) {
                File file = fileList.get(fileIndex);

                String mime = mimeTypeMap.getMimeTypeFromExtension(FileUtils.getFileExtension(file.getName()));
                MultipartBody.Part part = MultipartBody.Part.createFormData("xml_submission_file",file.getName(),multipartBody);

                multipartBody.parts().add(part);

                byteCount += file.length();
                Timber.i("added file of type '%s' %s", mime, file.getName());

                // we've added at least one attachment to the request...
                if (fileIndex + 1 < fileList.size()) {
                    if ((fileIndex - lastFileIndex + 1 > 100) || (byteCount + fileList.get(fileIndex + 1).length()
                            > 10000000L)) {
                        // the next file would exceed the 10MB threshold...
                        Timber.i("Extremely long post is being split into multiple posts");
                        multipartBody.parts().add(MultipartBody.Part.createFormData("*isIncomplete*", "yes"));
                        ++fileIndex; // advance over the last attachment added...
                        break;
                    }
                }
            }

            try {
                OkHttpClient client = createOkHttpClient();
                Request request = postRequest(uri, multipartBody);
                Response response = client.newCall(request).execute();

                messageParser = new ResponseMessageParser(
                        response.toString(),
                        response.code(),
                        response.message());

                discardEntityBytes(response);

                if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
// TODO:                 getCookieStore().clear();
                }

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

    @Override
    public void clearCookieStore() {

    }

    @Override
    public void clearHostCredentials(String host) {

    }

    @Override
    public void addCredentials(String username, String password, String host) {

    }

    private OkHttpClient createOkHttpClient() {

//        SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT).setSoTimeout(
//                2 * timeout)
//                .build();
//
//        // if possible, bias toward digest auth (may not be in 4.0 beta 2)
//        List<String> targetPreferredAuthSchemes = new ArrayList<>();
//        targetPreferredAuthSchemes.add(AuthSchemes.DIGEST);
//        targetPreferredAuthSchemes.add(AuthSchemes.BASIC);
//
//        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
//                .setConnectTimeout(timeout)
//                // support authenticating
//                .setAuthenticationEnabled(true)
//                // support redirecting to handle http: => https: transition
//                .setRedirectsEnabled(true)
//                .setMaxRedirects(1)
//                .setCircularRedirectsAllowed(true)
//                .setTargetPreferredAuthSchemes(targetPreferredAuthSchemes)
//                .setCookieSpec(CookieSpecs.DEFAULT)
//                .build();
//
//        return HttpClientBuilder.create()
//                .setDefaultSocketConfig(socketConfig)
//                .setDefaultRequestConfig(requestConfig)
//                .build();


        return new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build();
    }

    private String getUserAgentString () {
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
            InputStream is = body.byteStream();

            try {
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

    private Request getRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .get()
                .build();
    }

    private Request headRequest(@NonNull URI uri) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .head()
                .build();
    }

    private Request postRequest(@NonNull URI uri, RequestBody body) throws MalformedURLException {
        return new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING)
                .addHeader(USER_AGENT_HEADER, getUserAgentString())
                .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                .addHeader(DATE_HEADER, getHeaderDate())
                .post(body)
                .build();
    }


}
