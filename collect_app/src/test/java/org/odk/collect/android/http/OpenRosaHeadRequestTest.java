package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.http.support.MockWebServerRule;

import java.net.URI;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class OpenRosaHeadRequestTest {

    protected abstract OpenRosaHttpInterface buildSubject();

    @Rule
    public MockWebServerRule mockWebServerTester = new MockWebServerRule();

    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        subject = buildSubject();
    }

    @Test
    public void makesAHeadRequestToUri() throws Exception {
        MockWebServer mockWebServer = mockWebServerTester.startMockWebServer();
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        subject.executeHeadRequest(uri, null);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("HEAD"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void sendsCollectHeaders() throws Exception {
        MockWebServer mockWebServer = mockWebServerTester.startMockWebServer();
        mockWebServer.enqueue(new MockResponse());

        subject.executeHeadRequest(mockWebServer.url("").uri(), null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("User-Agent"), equalTo(String.format(
                "null %s/%s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME)));
    }

    @Test
    public void when204Response_returnsHeaders() throws Exception {
        MockWebServer mockWebServer = mockWebServerTester.startMockWebServer();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204)
                .addHeader("X-1", "Blah1")
                .addHeader("X-2", "Blah2"));

        HttpHeadResult result = subject.executeHeadRequest(mockWebServer.url("").uri(), null);
        assertThat(result.getHeaders().get("X-1"), equalTo("Blah1"));
        assertThat(result.getHeaders().get("X-2"), equalTo("Blah2"));
    }

    @Test
    public void whenRequestFails_throwsExceptionWithMessage() {
        try {
            subject.executeHeadRequest(new URI("http://localhost:8443"), null);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), not(isEmptyString()));
        }
    }
}