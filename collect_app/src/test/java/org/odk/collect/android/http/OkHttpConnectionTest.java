package org.odk.collect.android.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionTest extends OpenRosaHttpInterfaceTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(new OkHttpClient.Builder()
                .sslSocketFactory(TlsUtil.localhost().sslSocketFactory(), TlsUtil.localhost().trustManager())
        );
    }

    @Test
    public void executeGetRequest_withCredentials_whenHttps_retriesWithCredentials() throws Exception  {
        MockWebServer httpsMockWebServer = new MockWebServer();
        httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        httpsMockWebServer.start();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());

        buildSubject().executeGetRequest(httpsMockWebServer.url("").uri(), null, new HttpCredentials("user", "pass"));

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(2));
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }
}