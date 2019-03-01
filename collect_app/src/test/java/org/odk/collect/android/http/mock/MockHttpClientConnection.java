package org.odk.collect.android.http.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.http.HttpGetResult;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.HttpPostResult;
import org.odk.collect.android.http.OpenRosaHttpInterface;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockHttpClientConnection implements OpenRosaHttpInterface {

    @Override
    @NonNull
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {

        String xml =
                "<forms>" +
                        "<form url=\"https://opendatakit.appspot.com/formXml?formId=CascadingSelect\">Cascading Select Form</form>" +
                        "<form url=\"https://opendatakit.appspot.com/formXml?formId=widgets\">Widgets</form>" +
                        "<form url=\"https://opendatakit.appspot.com/formXml?formId=NewWidgets\">New Widgets</form>" +
                        "<form url=\"https://opendatakit.appspot.com/formXml?formId=sample\">sample</form>" +
                        "</forms>";

        InputStream is = new ByteArrayInputStream(xml.getBytes());

        Map<String, String> headers = new HashMap<>();
        headers.put("X-OpenRosa-Version", "1.0");
        headers.put("Content-Type", "text/xml;charset=utf-8");

        return new HttpGetResult(is, headers, "test-hash", HttpURLConnection.HTTP_OK);
    }

    @NonNull
    @Override
    public HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        return new HttpHeadResult(0, new HashMap<String, String>());
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws IOException {
        return new HttpPostResult("", 0, "");
    }

    @NonNull
    @Override
    public HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        return new HttpPostResult("", 0, "");

    }
}
