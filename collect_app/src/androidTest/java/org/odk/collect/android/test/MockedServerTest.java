package org.odk.collect.android.test;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.odk.collect.android.test.TestUtils.backupPreferences;
import static org.odk.collect.android.test.TestUtils.restorePreferences;

public abstract class MockedServerTest {
    private Map<String, ?> prefsBackup;

    protected MockWebServer server;

    @Before
    public void http_setUp() throws Exception {
        prefsBackup = backupPreferences();

        server = mockWebServer();
    }

    @After
    public void http_tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
        }

        if (prefsBackup != null) {
            restorePreferences(prefsBackup);
        }
    }

    protected void willRespondWith(String... rawResponses) {
        for (String rawResponse : rawResponses) {
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
    }

    protected RecordedRequest nextRequest() throws Exception {
        return server.takeRequest(1, TimeUnit.MILLISECONDS);
    }

   protected static String join(String... strings) {
        StringBuilder bob = new StringBuilder();
        for (String s : strings) {
            bob.append(s).append('\n');
        }
        return bob.toString();
    }

    private static MockWebServer mockWebServer() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        configAppFor(server);
        return server;
    }

    private static void configAppFor(MockWebServer server) {
        Editor prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext()).edit();
        prefs.putString(PreferenceKeys.KEY_SERVER_URL, server.url("/").toString());
        if (!prefs.commit()) {
            throw new RuntimeException("Failed to set up SharedPreferences for MockWebServer");
        }
    }
}
