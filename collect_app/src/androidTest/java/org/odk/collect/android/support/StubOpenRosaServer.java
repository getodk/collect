package org.odk.collect.android.support;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

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
import java.io.IOException;
import java.io.InputStream;
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

    private final List<FormManifestEntry> forms = new ArrayList<>();

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        if (uri.getPath().equals(formListPath)) {
            String response = getFormListResponse();

            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-openrosa-version", "1.0");
            return new HttpGetResult(new ByteArrayInputStream(response.getBytes()), headers, "", 200);
        } else if (uri.getPath().equals("/form")) {
            InputStream response = getFormResponse(uri);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-openrosa-version", "1.0");
            return new HttpGetResult(response, headers, "", 200);
        } else {
            return new HttpGetResult(null, new HashMap<>(), "", 404);
        }
    }

    @NotNull
    private InputStream getFormResponse(@NonNull URI uri) throws IOException {
        String formID = uri.getQuery().split("=")[1];
        String xmlPath = forms.get(Integer.parseInt(formID)).getFormXML();

        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        return assetManager.open("forms/" + xmlPath);
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

    public void addForm(String formLabel, String formXML) {
        forms.add(new FormManifestEntry(formLabel, formXML));
    }

    public String getURL() {
        return URL;
    }

    @NotNull
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private String getFormListResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
                .append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n");

        for (int i = 0; i < forms.size(); i++) {
            FormManifestEntry form = forms.get(i);

            stringBuilder
                    .append("<xform>\n")
                    .append("<formID>" + i + "</formID>\n")
                    .append("<name>" + form.getFormLabel() + "</name>\n")
                    .append("<version>1</version>\n")
                    .append("<hash>md5:c28fc778a9291672badee04ac880a05d</hash>\n")
                    .append("<downloadUrl>" + getURL() + "/form?formId=" + i + "</downloadUrl>\n")
                    .append("</xform>\n");
        }

        stringBuilder.append("</xforms>");
        return stringBuilder.toString();
    }

    private static class FormManifestEntry {

        private final String formLabel;
        private final String formXML;

        FormManifestEntry(String formLabel, String formXML) {
            this.formLabel = formLabel;
            this.formXML = formXML;
        }

        public String getFormLabel() {
            return formLabel;
        }

        public String getFormXML() {
            return formXML;
        }
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
