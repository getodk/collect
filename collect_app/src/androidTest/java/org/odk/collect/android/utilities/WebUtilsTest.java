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
        // when
        doRequest("/some-path");

        // then
        RecordedRequest r = server.takeRequest(1, TimeUnit.MILLISECONDS);
        assertEquals("GET /some-path HTTP/1.1", r.getRequestLine());
        assertTrue(r.getHeader("User-Agent").matches("Dalvik/.* org.odk.collect.android/.*"));
    }

    @Test
    public void getXmlDocument_request_shouldSupplyExpectedHeaders() throws Exception {
        // when
        WebUtils.getXmlDocument(url("/list-forms"), httpContext(), httpClient());

        // then
        RecordedRequest r = server.takeRequest(1, TimeUnit.MILLISECONDS);
        assertEquals("GET /list-forms HTTP/1.1", r.getRequestLine());
        assertEquals(6, r.getHeaders().size());
        assertTrue(r.getHeader("User-Agent").matches("Dalvik/.* org.odk.collect.android/.*"));
    }

    private static String url(String path) {
        String url = String.format("http://uname:pword@localhost:%s%s", server.getPort(), path);
    }

    private static void doRequest(String path) throws Exception {
        HttpGet req = WebUtils.createOpenRosaHttpGet(new URI(url(path)));
        HttpResponse response = httpClient().execute(req, httpContext());
    }

    private static HttpClient httpClient() {
        return WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
    }

    private static HttpContext httpContext() {
        return Collect.getInstance().getHttpContext();
    }
}
