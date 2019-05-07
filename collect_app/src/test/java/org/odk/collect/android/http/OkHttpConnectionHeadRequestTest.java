package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    private MockWebServer httpsMockWebServer;

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(new OkHttpClient.Builder()
                .sslSocketFactory(TlsUtil.localhost().sslSocketFactory(), TlsUtil.localhost().trustManager())
        );
    }

    @After
    public void shutdownMockWebserver() throws IOException {
        if (httpsMockWebServer != null) {
            httpsMockWebServer.shutdown();
        }
    }

    @Test
    public void withCredentials_whenHttps_retriesWithCredentials() throws Exception  {
        httpsMockWebServer = new MockWebServer();
        httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        httpsMockWebServer.start(8443);

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\""));
        httpsMockWebServer.enqueue(new MockResponse());

        buildSubject().executeHeadRequest(httpsMockWebServer.url("").uri(), new HttpCredentials("user", "pass"));

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(2));
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }
}
