package org.odk.collect.android.http.support;

import org.jetbrains.annotations.NotNull;

import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

public class MockWebServerHelper {

    private MockWebServerHelper() {

    }

    @NotNull
    public static Request buildRequest(MockWebServer mockWebServer, String path) {
        return new Request.Builder().url(mockWebServer.url(path)).build();
    }
}
