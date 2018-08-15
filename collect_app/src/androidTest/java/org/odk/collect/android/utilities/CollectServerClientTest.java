package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.test.MockedServerTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.odk.collect.android.test.TestUtils.assertMatches;

public class CollectServerClientTest extends MockedServerTest {

    private CollectServerClient collectServerClient;

    @Before
    public void setUp() throws Exception {
        // server hangs without a response queued:
        server.enqueue(new MockResponse());
        collectServerClient = new CollectServerClient(new HttpClientConnection(), new WebCredentialsUtils());
    }

        @Test
        public void httpRequests_shouldHaveUseragentHeader() throws Exception {
            // when
            doRequest(url("/some-path"));

            // then
            RecordedRequest r = nextRequest();
            assertEquals("GET /some-path HTTP/1.1", r.getRequestLine());
            assertTrue(r.getHeader("User-Agent").matches("Dalvik/.* org.odk.collect.android/.*"));
        }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_UserAgent() throws Exception {
        // when
        collectServerClient.getXmlDocument(url("/list-forms"));

        // then
        String header = nextRequest().getHeader("User-Agent");

        assertMatches("Dalvik/.* org.odk.collect.android/.*", header);
    }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_X_OpenRosa_Version() throws Exception {
        // when
        collectServerClient.getXmlDocument(url("/list-forms"));

        // then
        assertEquals("1.0",
                nextRequest().getHeader("X-OpenRosa-Version"));
    }

    @Test
    public void getXmlDocument_request_shouldSupplyHeader_AcceptEncoding_gzip() throws Exception {
        // when
        collectServerClient.getXmlDocument(url("/list-forms"));

        // then
        assertEquals("gzip",
                nextRequest().getHeader("Accept-Encoding"));
    }

    @Test
    public void getXmlDocument_request_shouldNotSupplyHeader_Authorization_forHttpRequest() throws Exception {
        // when
        collectServerClient.getXmlDocument(url("/list-forms"));

        // then
        assertNull(nextRequest().getHeader("Authorization"));
    }

    @Test
    public void getXmlDocument_request_shouldReportInvalidUrl() throws Exception {
        // when
        DocumentFetchResult res = collectServerClient.getXmlDocument("NOT_A_URL");

        // then
        assertEquals(0, res.responseCode);
        assertMatches(".*while accessing NOT_A_URL", res.errorMessage);
    }

    @Test
    public void getXmlDocument_request_shouldReportInvalidHost() throws Exception {
        // when
        DocumentFetchResult res = collectServerClient.getXmlDocument("file:/some/path");

        // then
        assertEquals(0, res.responseCode);
        assertEquals("Parsing failed with Invalid server URL (no hostname): file:/some/path while accessing file:/some/path", res.errorMessage);
    }

    private String url(String path) {
        return server.url(path).toString();
    }

    private void doRequest(String path) throws Exception {
        collectServerClient.getHttpInputStream(path, null);
    }

}
