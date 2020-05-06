package org.odk.collect.android.support;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.openrosa.CaseInsensitiveEmptyHeaders;
import org.odk.collect.android.openrosa.CaseInsensitiveHeaders;
import org.odk.collect.android.openrosa.HttpCredentialsInterface;
import org.odk.collect.android.openrosa.HttpGetResult;
import org.odk.collect.android.openrosa.HttpHeadResult;
import org.odk.collect.android.openrosa.HttpPostResult;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class StubOpenRosaServer implements OpenRosaHttpInterface {

    private static final String URL = "https://server.example.com";

    private String formListPath = "/formList";
    private String submissionPath = "/submission";

    private final List<String> forms = new ArrayList<>();

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        if (uri.getPath().equals(formListPath)) {
            String response = getFormListResponse();
            return new HttpGetResult(new ByteArrayInputStream(response.getBytes()), new HashMap<>(), "", 200);
        } else {
            return new HttpGetResult(null, new HashMap<>(), "", 404);
        }
    }

    @NonNull
    @Override
    public HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        if (uri.getPath().equals(submissionPath)) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-openrosa-accept-content-length", "10485760");

            return new HttpHeadResult(204, new MapHeaders(headers));
        } else {
            return new HttpHeadResult(404, new CaseInsensitiveEmptyHeaders());
        }
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws Exception {
        if (uri.getPath().equals(submissionPath)) {
            return new HttpPostResult("", 201, "");
        } else {
            return new HttpPostResult("", 404, "");
        }
    }

    public void setFormListPath(String path) {
        formListPath = path;
    }

    public void setFormSubmissionPath(String path) {
        submissionPath = path;
    }

    public void addForm(String formLabel) {
        forms.add(formLabel);
    }

    public String getURL() {
        return URL;
    }

    @NotNull
    private String getFormListResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<forms>\n");

        for (String form : forms) {
            stringBuilder
                    .append("<form url=\"https://server.example.com/formXml?formId=customPathForm\">")
                    .append(form)
                    .append("</form>\n");
        }

        stringBuilder.append("</forms>");
        return stringBuilder.toString();
    }

    private static class MapHeaders implements CaseInsensitiveHeaders {

        private final Map<String, String> headers;

        MapHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        @javax.annotation.Nullable
        @Override
        public Set<String> getHeaders() {
            return headers.keySet();
        }

        @Override
        public boolean containsHeader(String header) {
            return headers.containsKey(header.toLowerCase(Locale.ENGLISH));
        }

        @javax.annotation.Nullable
        @Override
        public String getAnyValue(String header) {
            return headers.get(header.toLowerCase(Locale.ENGLISH));
        }

        @javax.annotation.Nullable
        @Override
        public List<String> getValues(String header) {
            return asList(headers.get(header.toLowerCase(Locale.ENGLISH)));
        }
    }
}
