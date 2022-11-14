package org.odk.collect.android.openrosa.okhttp;

import com.google.gson.Gson;        // smap
import com.google.gson.GsonBuilder; // smap

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.openrosa.CaseInsensitiveEmptyHeaders;
import org.odk.collect.android.openrosa.CaseInsensitiveHeaders;
import org.odk.collect.android.openrosa.HttpCredentialsInterface;
import org.odk.collect.android.openrosa.HttpGetResult;
import org.odk.collect.android.openrosa.HttpHeadResult;
import org.odk.collect.android.openrosa.HttpPostResult;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.OpenRosaServerClient;
import org.odk.collect.android.taskModel.TaskResponse;    // smap
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;    // smap
import java.io.File;
import java.io.IOException;    // smap
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.FormBody;    // smap
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpConnection implements OpenRosaHttpInterface {

    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";     // smap
    private static final String GZIP_CONTENT_ENCODING = "gzip";                 // smap
    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    private final OkHttpOpenRosaServerClientProvider clientFactory;

    @NonNull
    private final FileToContentTypeMapper fileToContentTypeMapper;

    @NonNull
    private final String userAgent;

    public OkHttpConnection(@NonNull OkHttpOpenRosaServerClientProvider clientFactory, @NonNull FileToContentTypeMapper fileToContentTypeMapper, @NonNull String userAgent) {
        this.clientFactory = clientFactory;
        this.fileToContentTypeMapper = fileToContentTypeMapper;
        this.userAgent = userAgent;
    }

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());  // smap add host
        Request request = new Request.Builder()
                .url(uri.toURL())
                .get()
                .build();

        Response response = httpClient.makeRequest(request, new Date());
        int statusCode = response.code();

        if (statusCode != HttpURLConnection.HTTP_OK) {
            discardEntityBytes(response);
            Timber.i("Error: %s (%s at %s", response.message(), String.valueOf(statusCode), uri.toString());

            String errMsg = response.message() +  " : " + String.valueOf(statusCode) +  " : " + uri.toString();     // smap
            throw new Exception(errMsg);    // smap
            //return new HttpGetResult(null, new HashMap<String, String>(), "", statusCode);    // smap
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
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());  // smap add host
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
    public HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials,
                                               String status,              // smap
                                               String location_trigger,    // smap
                                               String survey_notes,        // smap
                                               String assignment_id,       // smap
                                               @NonNull long contentLength) throws Exception {
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


            // Start Smap
            if(survey_notes != null) {
                multipartBuilder.addPart(MultipartBody.Part.createFormData("survey_notes", survey_notes));
            }
            if(location_trigger != null) {
                multipartBuilder.addPart(MultipartBody.Part.createFormData("location_trigger", location_trigger));
            }
            if(assignment_id != null) {
                multipartBuilder.addPart(MultipartBody.Part.createFormData("assignment_id", assignment_id));
            }
            // end smap

            MultipartBody multipartBody = multipartBuilder.build();
            postResult = executePostRequest(uri, credentials, multipartBody);

            if (postResult.getResponseCode() != HttpURLConnection.HTTP_CREATED &&
                    postResult.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                return postResult;
            }

        }

        return postResult;
    }

    @NonNull
    private HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials, MultipartBody multipartBody) throws Exception {
        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());    // smap add host
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
                //Timber.i(e);      // Smap I don't think we need to know if this fails probably because the stream is closed
            }
        }
    }

    /*
     * Begin smap
     */
    @Override
    public @NonNull HttpPostResult uploadTaskStatus(@NonNull TaskResponse updateResponse,
                                                    @NonNull URI uri,
                                                    @Nullable HttpCredentialsInterface credentials
    ) throws IOException {

        // debug
        //for(PointEntry p : updateResponse.userTrail) {
        //    Timber.i(String.valueOf(p.time));
        //}
        Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm").create();
        String resp = gson.toJson(updateResponse);
        Timber.i("########################################### %s", resp);

        RequestBody formBody = new FormBody.Builder()
                .add("assignInput", resp)
                .build();

        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());

        HttpPostResult postResult;
        Request request = new Request.Builder()
                .url(uri.toURL())
                .post(formBody)
                .build();

        Response response = httpClient.makeRequest(request, new Date());

        if (response.code() == 204) {
            throw new IOException();
        }

        postResult = new HttpPostResult(
                response.body().string(),
                response.code(),
                response.message());

        discardEntityBytes(response);

        return postResult;

    }

    @Override
    public @NonNull HttpPostResult uploadLocation(String lat,
                                                  String lon,
                                                  @NonNull URI uri,
                                                  @Nullable HttpCredentialsInterface credentials
    ) throws IOException {

        RequestBody formBody = new FormBody.Builder()
                .add("lat", lat)
                .add("lon", lon)
                .build();

        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());

        HttpPostResult postResult;
        Request request = new Request.Builder()
                .url(uri.toURL())
                .post(formBody)
                .build();

        Response response = httpClient.makeRequest(request, new Date());

        if (response.code() != 204) {
            Timber.e(new Exception(response.message()));
        }

        postResult = new HttpPostResult(
                response.body().string(),
                response.code(),
                response.message());

        discardEntityBytes(response);

        return postResult;

    }

    @Override
    public @NonNull String SubmitFileForResponse(@NonNull String fileName,
                                                 @NonNull File file,
                                                 @NonNull URI uri,
                                                 @Nullable HttpCredentialsInterface credentials) throws IOException {

        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());  // smap add host

        String contentType = fileToContentTypeMapper.map(file.getName());
        RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), file);

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
        multipartBuilder.addPart(MultipartBody.Part.createFormData(file.getName(), file.getName(), requestBody));
        MultipartBody mpBody = multipartBuilder.build();

        Request request = new Request.Builder()
                .url(uri.toURL())
                .post(mpBody)
                .build();

        Response response = httpClient.makeRequest(request, new Date());

        int code = response.code();
        String body = response.body().string();
        Timber.i("%%%%%%%%%%%%%%%%%%%%: %s", body);
        String resp = null;
        if(code == 201 || code == 200) {
            resp = body;
        } else {
            resp = code + ": " + response.message();
        }

        return resp;
    }

    @Override
    public @NonNull
    String getRequest(@NonNull URI uri, @Nullable final String contentType,
                      @Nullable HttpCredentialsInterface credentials,
                      HashMap<String, String> headers) throws Exception {

        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost()); // smap add host

        Request.Builder b = new Request.Builder()
                .url(uri.toURL());

        // Add headers
        if(!headers.isEmpty()) {
            for(String key : headers.keySet()) {
                b.addHeader(key, headers.get(key));
            }
        }
        Request request = b.get().build();

        Response response = httpClient.makeRequest(request, new Date());
        int statusCode = response.code();
        ByteArrayOutputStream os = null;

        if (statusCode != HttpURLConnection.HTTP_OK) {
            discardEntityBytes(response);
            String errMsg = response.message() +  " : " + String.valueOf(statusCode) +  " : " + uri.toString();
            //String errMsg = Collect
            //        .getInstance()
            //        .getString(R.string.file_fetch_failed, uri.toString(), response.message(), String.valueOf(statusCode));

            Timber.e(errMsg);
            throw new Exception(errMsg);    // smap
            //return new HttpGetResult(null, new HashMap<String, String>(), "", statusCode);    // smap
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

        return body.string();
    }

    @Override
    public @NonNull
    String loginRequest(@NonNull URI uri, @Nullable final String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {

        OpenRosaServerClient httpClient = clientFactory.get(uri.getScheme(), userAgent, credentials, uri.getHost());  // smap add host

        Request request = new Request.Builder()
                .url(uri.toURL())
                .addHeader(ACCEPT_ENCODING_HEADER, GZIP_CONTENT_ENCODING)
                .get()
                .build();

        Response response = httpClient.makeRequest(request, new Date());
        int statusCode = response.code();
        ByteArrayOutputStream os = null;

        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return "unauthorized";

        } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            // Treat not found as "success" except for hosts that are known to support the login service
            String host = uri.getHost();
            if(host.equals("app.kontrolid.com") || host.endsWith("smap.com.au")) {
                return "error";
            } else {
                return "success";
            }

        } else if (statusCode == HttpURLConnection.HTTP_OK) {
            return "success";

        } else {
            return "error";
        }
    }

    /*
     * End smap
     */

}
