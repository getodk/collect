package org.odk.collect.android.utilities;

import okhttp3.mockwebserver.*;

import java.net.*;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.odk.collect.android.application.*;
import org.opendatakit.httpclientandroidlib.*;
import org.opendatakit.httpclientandroidlib.client.*;
import org.opendatakit.httpclientandroidlib.client.methods.*;
import org.opendatakit.httpclientandroidlib.protocol.*;

import static org.junit.Assert.*;

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
