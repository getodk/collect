package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionTest extends OpenRosaHttpInterfaceTest {

    @Override
    protected void startHttpsMockWebServer(MockWebServer mockWebServer) throws IOException {
        mockWebServer.start(8443);
    }

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection();
    }
}
