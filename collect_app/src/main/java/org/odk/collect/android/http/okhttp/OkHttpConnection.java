package org.odk.collect.android.http.okhttp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.http.HttpGetResult;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.HttpPostResult;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.http.openrosa.OpenRosaServerClient;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpConnection implements OpenRosaHttpInterface {

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    /**
     * Shared client object used for all HTTP requests. Credentials are set on a per-request basis.
     */
    private OpenRosaServerClient httpClient;

    /**
     * The credentials used for the last request. When a new request is made, this is used to see
     * whether the {@link #httpClient} credentials need to be changed.
     */
    private HttpCredentialsInterface lastRequestCredentials;

    /**
     * The scheme used for the last request. When a new request is made, this is used to see
     * whether the {@link #httpClient} credentials need to be changed.
     */
    private String lastRequestScheme = "";

    private MultipartBody multipartBody;

    private final OkHttpOpenRosaServerClientFactory clientFactory;

    @NonNull
    private final FileToContentTypeMapper fileToContentTypeMapper;

    public OkHttpConnection(@NonNull OkHttpOpenRosaServerClientFactory clientFactory, @NonNull FileToContentTypeMapper fileToContentTypeMapper) {
        this.clientFactory = clientFactory;
        this.fileToContentTypeMapper = fileToContentTypeMapper;
    }

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        createClient(credentials, uri.getScheme());
        Request request = new Request.Builder()
                .url(uri.toURL())
                .get()
                .build();

        Response response = httpClient.makeRequest(request);
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
        createClient(credentials, uri.getScheme());
        Request request = new Request.Builder()
                .url(uri.toURL())
                .head()
                .build();

        Timber.i("Issuing HEAD request to: %s", uri.toString());
        Response response = httpClient.makeRequest(request);
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
    public HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws Exception {
        HttpPostResult postResult = null;

        boolean first = true;
        int fileIndex = 0;
        int lastFileIndex;
        while (fileIndex < fileList.size() || first) {
            lastFileIndex = fileIndex;
            first = false;
            long byteCount = 0L;

            RequestBody requestBody = RequestBody.create(MediaType.parse(HTTP_CONTENT_TYPE_TEXT_XML), submissionFile);

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(MultipartBody.Part.createFormData("xml_submission_file", submissionFile.getName(), requestBody));

            Timber.i("added xml_submission_file: %s", submissionFile.getName());
            byteCount += submissionFile.length();

            for (; fileIndex < fileList.size(); fileIndex++) {
                File file = fileList.get(fileIndex);

                String contentType = fileToContentTypeMapper.map(file.getName());

                RequestBody fileRequestBody = RequestBody.create(MediaType.parse(contentType), file);
                multipartBuilder.addPart(MultipartBody.Part.createFormData(file.getName(), file.getName(), fileRequestBody));

                byteCount += file.length();
                Timber.i("added file of type '%s' %s", contentType, file.getName());

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

    @NonNull
    private HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        createClient(credentials, uri.getScheme());
        HttpPostResult postResult;
        Request request = new Request.Builder()
                .url(uri.toURL())
                .post(multipartBody)
                .build();
        Response response = httpClient.makeRequest(request);

        if (response.code() == 204) {
            throw new Exception();
        }

        postResult = new HttpPostResult(
                response.body().string(),
                response.code(),
                response.message());

        discardEntityBytes(response);

        return postResult;
    }

    /**
     * If the provided credentials are non-null, sets the {@link #httpClient} to authenticate using
     * the provided credential and sets the {@link #lastRequestCredentials}
     * <p>
     * If authentication is needed, always configure digest auth. If SSL is enabled, also configure
     * basic auth.
     */
    private void createClient(@Nullable HttpCredentialsInterface credentials, String scheme) {
        if (credentials != null && !(credentials.equals(lastRequestCredentials) && scheme.equals(lastRequestScheme))) {
            httpClient = clientFactory.create(scheme, Collect.getInstance().getUserAgentString(), credentials);
            lastRequestCredentials = credentials;
            lastRequestScheme = scheme;
        } else if (httpClient == null) {
            httpClient = clientFactory.create(scheme, Collect.getInstance().getUserAgentString(), credentials);
        }
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
