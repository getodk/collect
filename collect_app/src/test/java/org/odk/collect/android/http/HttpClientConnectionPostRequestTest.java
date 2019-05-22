package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionPostRequestTest extends OpenRosaPostRequestTest {

    private MockWebServer httpsPortMockWebServer;

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection();
    }

    @After
    public void shutdownMockWebserver() throws IOException {
        if (httpsPortMockWebServer != null) {
            httpsPortMockWebServer.shutdown();
        }
    }

    @Test
    public void withCredentials_whenHttpsPort_retriesWithCredentials() throws Exception  {
        httpsPortMockWebServer = new MockWebServer();
        httpsPortMockWebServer.start(8443);

        httpsPortMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsPortMockWebServer.enqueue(new MockResponse());

        URI uri = httpsPortMockWebServer.url("").uri();
        buildSubject().uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, new HttpCredentials("user", "pass"), 0);

        assertThat(httpsPortMockWebServer.getRequestCount(), equalTo(2));
        httpsPortMockWebServer.takeRequest();
        RecordedRequest request = httpsPortMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }
}
