package org.odk.collect.android.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionTest extends OpenRosaHttpInterfaceTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection();
    }

    @Test
    public void executeGetRequest_withCredentials_whenHttpsPort_retriesWithCredentials() throws Exception  {
        MockWebServer httpsPortMockWebServer = new MockWebServer();
        httpsPortMockWebServer.start(8443);

        httpsPortMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsPortMockWebServer.enqueue(new MockResponse());

        buildSubject().executeGetRequest(httpsPortMockWebServer.url("").uri(), null, new HttpCredentials("user", "pass"));

        assertThat(httpsPortMockWebServer.getRequestCount(), equalTo(2));
        httpsPortMockWebServer.takeRequest();
        RecordedRequest request = httpsPortMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }
}
