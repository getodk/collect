package org.odk.collect.android.utilities;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.odk.collect.android.application.Collect;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebUtilsTest {
    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        // server hangs without a response queued:
        server.enqueue(new MockResponse());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void httpRequests_shouldHaveUseragentHeader() throws Exception {
        // given
        String url = String.format("http://uname:pword@localhost:%s/some-path", server.getPort());

        // when
        doRequest(url);

        // then
        RecordedRequest r = server.takeRequest(1, TimeUnit.MILLISECONDS);
        assertEquals("GET /some-path HTTP/1.1", r.getRequestLine());
        assertTrue(r.getHeader("User-Agent").matches("Dalvik/.* org.odk.collect.android/.*"));
    }

    private static void doRequest(String url) throws Exception {
        HttpContext localContext = Collect.getInstance().getHttpContext();
        HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
        HttpGet req = WebUtils.createOpenRosaHttpGet(new URI(url));
        HttpResponse response = httpclient.execute(req, localContext);
    }
}
