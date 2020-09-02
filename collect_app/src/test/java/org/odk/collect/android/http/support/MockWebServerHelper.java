package org.odk.collect.android.http.support;

import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

public class MockWebServerHelper {

    private MockWebServerHelper() {

    }

    public static Request buildRequest(MockWebServer mockWebServer, String path) {
        return new Request.Builder().url(mockWebServer.url(path)).build();
    }
}
