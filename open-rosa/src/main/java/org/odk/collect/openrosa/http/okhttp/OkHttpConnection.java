package org.odk.collect.openrosa.http.okhttp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.odk.collect.openrosa.http.CaseInsensitiveEmptyHeaders;
import org.odk.collect.openrosa.http.CaseInsensitiveHeaders;
import org.odk.collect.openrosa.http.HttpCredentialsInterface;
import org.odk.collect.openrosa.http.HttpGetResult;
import org.odk.collect.openrosa.http.HttpHeadResult;
import org.odk.collect.openrosa.http.HttpPostResult;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.openrosa.http.SubmissionChunker;
import org.odk.collect.openrosa.http.SubmissionUploadProgressTracker;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
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

    private final OkHttpOpenRosaServerClientProvider clientFactory;

    @NonNull
    private final FileToContentTypeMapper fileToContentTypeMapper;

    @NonNull
    private final String userAgent;

    public OkHttpConnection(@Nullable String cacheDir, @NonNull FileToContentTypeMapper fileToContentTypeMapper, @NonNull String userAgent) {
        this.clientFactory = new OkHttpOpenRosaServerClientProvider(cacheDir);
        this.fileToContentTypeMapper = fileToContentTypeMapper;
        this.userAgent = userAgent;
    }

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials);
        Request request = new Request.Builder()
                .url(uri.toURL())
                .get()
                .build();

        Response response = httpClient.makeRequest(request, new Date());
        int statusCode = response.code();

        if (statusCode != HttpURLConnection.HTTP_OK) {
            discardEntityBytes(response);
            Timber.i("Error: %s (%s at %s", response.message(), String.valueOf(statusCode), uri.toString());

            return new HttpGetResult(null, new HashMap<>(), "", statusCode);
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
            hash = Md5.getMd5Hash(new ByteArrayInputStream(bytes));
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
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials);
        Request request = new Request.Builder()
                .url(uri.toURL())
                .head()
                .build();

        Timber.i("Issuing HEAD request to: %s", uri.toString());
        Response response = httpClient.makeRequest(request, new Date());
        int statusCode = response.code();

        CaseInsensitiveHeaders responseHeaders = new CaseInsensitiveEmptyHeaders();

        if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
            responseHeaders = new OkHttpCaseInsensitiveHeaders(response.headers());
        }

        discardEntityBytes(response);

        return new HttpHeadResult(statusCode, responseHeaders);
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionAndFiles(@NonNull File submissionFile, @NonNull List<File> fileList, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws Exception {
        return uploadSubmissionAndFiles(submissionFile, fileList, uri, credentials, contentLength, null);
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionAndFiles(@NonNull File submissionFile, @NonNull List<File> fileList, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength, @Nullable SubmissionUploadProgressTracker progressTracker) throws Exception {
        List<SubmissionChunker.Chunk> chunks = new SubmissionChunker(submissionFile.length(), fileList, contentLength).chunk();

        // Resume from the last chunk the server accepted (if the tracker says so). A resume index
        // beyond the last chunk is clamped so we always re-send the final, submission-finalizing
        // chunk. A null tracker always starts from the first chunk.
        int startChunk = 0;
        if (progressTracker != null) {
            startChunk = Math.max(0, Math.min(progressTracker.getResumeFromChunkIndex(), chunks.size() - 1));
        }
        if (startChunk > 0) {
            Timber.i("Resuming submission upload from chunk %d of %d", startChunk, chunks.size());
        }

        HttpPostResult postResult = null;
        for (int chunkIndex = startChunk; chunkIndex < chunks.size(); chunkIndex++) {
            SubmissionChunker.Chunk chunk = chunks.get(chunkIndex);

            RequestBody requestBody = RequestBody.create(MediaType.parse(HTTP_CONTENT_TYPE_TEXT_XML), submissionFile);

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(MultipartBody.Part.createFormData("xml_submission_file", submissionFile.getName(), requestBody));

            Timber.i("added xml_submission_file: %s", submissionFile.getName());

            for (int fileIndex = chunk.getStartIndex(); fileIndex < chunk.getEndIndex(); fileIndex++) {
                File file = fileList.get(fileIndex);

                String contentType = fileToContentTypeMapper.map(file.getName());

                RequestBody fileRequestBody = RequestBody.create(MediaType.parse(contentType), file);
                multipartBuilder.addPart(MultipartBody.Part.createFormData(file.getName(), file.getName(), fileRequestBody));

                Timber.i("added file of type '%s' %s", contentType, file.getName());
            }

            if (chunk.isIncomplete()) {
                // more chunks follow this one, so the server must keep the submission open...
                Timber.i("Extremely long post is being split into multiple posts");
                multipartBuilder.addPart(MultipartBody.Part.createFormData("*isIncomplete*", "yes"));
            }

            MultipartBody multipartBody = multipartBuilder.build();
            postResult = executePostRequest(uri, credentials, multipartBody);

            if (postResult.getResponseCode() != HttpURLConnection.HTTP_CREATED &&
                    postResult.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                // this chunk was not accepted: stop and do not record it as uploaded so the next
                // retry re-attempts from here...
                return postResult;
            }

            if (progressTracker != null) {
                progressTracker.onChunkUploaded(chunkIndex);
            }
        }

        return postResult;
    }

    @NonNull
    private HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials, MultipartBody multipartBody) throws Exception {
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials);
        HttpPostResult postResult;
        Request request = new Request.Builder()
                .url(uri.toURL())
                .post(multipartBody)
                .build();
        Response response = httpClient.makeRequest(request, new Date());

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
