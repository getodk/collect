package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.test.MockedServerTest;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.net.URI;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.test.TestUtils.assertMatches;

public class WebUtilsTest extends MockedServerTest {
    @Before
    public void setUp() throws Exception {
        // server hangs without a response queued:
        server.enqueue(new MockResponse());
    }

    @Test
    public void httpRequests_shouldHaveUseragentHeader() throws Exception {
        // when
        doRequest("/some-path");

        // then
        RecordedRequest r = nextRequest();
        assertEquals("GET /some-path HTTP/1.1", r.getRequestLine());
        assertTrue(r.getHeader("User-Agent").matches("Dalvik/.* org.odk.collect.android/.*"));
    }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_UserAgent() throws Exception {
        // when
        WebUtils.getXmlDocument(url("/list-forms"), httpContext(), httpClient());

        // then
        assertMatches("Dalvik/.* org.odk.collect.android/.*",
                nextRequest().getHeader("User-Agent"));
    }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_X_OpenRosa_Version() throws Exception {
        // when
        WebUtils.getXmlDocument(url("/list-forms"), httpContext(), httpClient());

        // then
        assertEquals("1.0",
                nextRequest().getHeader("X-OpenRosa-Version"));
    }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_AcceptEncoding_gzip() throws Exception {
        // when
        WebUtils.getXmlDocument(url("/list-forms"), httpContext(), httpClient());

        // then
        assertEquals("gzip",
                nextRequest().getHeader("Accept-Encoding"));
    }

    @Test
    public void getXmlDocument_request_shouldNotSupplyHeader_Authorization_forHttpRequest() throws Exception {
        // when
        WebUtils.getXmlDocument(url("/list-forms"), httpContext(), httpClient());

        // then
        assertNull(nextRequest().getHeader("Authorization"));
    }

    @Test
    public void getXmlDocument_request_shouldReportInvalidUrl() throws Exception {
        // when
        DocumentFetchResult res = WebUtils.getXmlDocument("NOT_A_URL", httpContext(), httpClient());

        // then
        assertEquals(0, res.responseCode);
        assertMatches(".*while accessingNOT_A_URL", res.errorMessage);
    }

    @Test
    public void getXmlDocument_request_shouldReportInvalidHost() throws Exception {
        // when
        DocumentFetchResult res = WebUtils.getXmlDocument("file:/some/path", httpContext(), httpClient());

        // then
        assertEquals(0, res.responseCode);
        assertEquals("Invalid server URL (no hostname): file:/some/path", res.errorMessage);
    }

    private String url(String path) {
        return server.url(path).toString();
    }

    private void doRequest(String path) throws Exception {
        HttpGet req = WebUtils.createOpenRosaHttpGet(new URI(url(path)));
        httpClient().execute(req, httpContext());
    }

    private static HttpClient httpClient() {
        return WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
    }

    private static HttpContext httpContext() {
        return Collect.getInstance().getHttpContext();
    }
}
