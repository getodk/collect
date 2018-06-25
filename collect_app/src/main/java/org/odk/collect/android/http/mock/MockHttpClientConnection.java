package org.odk.collect.android.http.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.http.HttpInputStreamResult;
import org.odk.collect.android.http.HttpInterface;
import org.odk.collect.android.utilities.ResponseMessageParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockHttpClientConnection implements HttpInterface {

    private boolean getHttpShouldReturnNull = false;

    @NonNull
    @Override
    public HttpInputStreamResult getHttpInputStream(@NonNull URI uri, @Nullable String contentType) throws Exception {
        if (getHttpShouldReturnNull) {
            return null;
        }

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

        return new HttpInputStreamResult(is, headers, "test-hash");
    }

    @Override
    public int httpHeadRequest(@NonNull URI uri, @NonNull Map<String, String> responseHeaders) throws Exception {
        return 0;
    }

    @Override
    public ResponseMessageParser uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri) throws IOException {
        return null;
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


    /**
     * Configuration methods for testing
     */
    public void setGetHttpShouldReturnNull(boolean shouldReturnNull) {
        this.getHttpShouldReturnNull = shouldReturnNull;
    }
}
