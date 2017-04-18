package org.odk.collect.android.test;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public final class MockedServerTestUtils {
    private MockedServerTestUtils() {}

    public static MockWebServer mockWebServer() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        configAppFor(server);
        return server;
    }

    public static void willRespond(MockWebServer server, String rawResponse) {
        MockResponse response = new MockResponse();

        String[] parts = rawResponse.split("\r\n\r\n", 2);

        String[] headerLines = parts[0].split("\r\n");

        response.setStatus(headerLines[0]);

        for (int i = 1; i < headerLines.length; ++i) {
            String[] headerParts = headerLines[i].split(": ", 2);
            response.addHeader(headerParts[0], headerParts[1]);
        }

        response.setBody(parts[1]);

        server.enqueue(response);
    }

    public static RecordedRequest firstRequestFor(MockWebServer server) throws Exception {
        return server.takeRequest(1, TimeUnit.MILLISECONDS);
    }

    private static void configAppFor(MockWebServer server) {
        Editor prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext()).edit();
        prefs.putString(PreferenceKeys.KEY_SERVER_URL, server.url("/").toString());
        if (!prefs.commit()) {
            throw new RuntimeException("Failed to set up SharedPreferences for MockWebServer");
        }
    }
}
