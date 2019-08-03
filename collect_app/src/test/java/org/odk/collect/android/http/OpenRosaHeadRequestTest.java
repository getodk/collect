package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.BuildConfig;

import java.io.IOException;
import java.net.URI;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class OpenRosaHeadRequestTest {

    protected abstract OpenRosaHttpInterface buildSubject();

    protected abstract Boolean useRealHttps();

    private final MockWebServer mockWebServer = new MockWebServer();
    private MockWebServer httpsMockWebServer;
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        subject = buildSubject();
    }

    @After
    public void teardown() throws Exception {
        mockWebServer.shutdown();

        if (httpsMockWebServer != null) {
            httpsMockWebServer.shutdown();
            httpsMockWebServer = null;
        }
    }

    @Test
    public void makesAHeadRequestToUri() throws Exception {
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
        mockWebServer.enqueue(new MockResponse());

        subject.executeHeadRequest(mockWebServer.url("").uri(), null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("User-Agent"), equalTo(String.format(
                "null %s/%s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME)));
    }

    @Test
    public void sendsOpenRosaHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeHeadRequest(mockWebServer.url("").uri(), null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-OpenRosa-Version"), equalTo("1.0"));
    }

    @Test
    public void whenLastRequestSetCookies_nextRequestDoesNotSendThem() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Set-Cookie", "blah=blah"));
        mockWebServer.enqueue(new MockResponse());

        subject.executeHeadRequest(mockWebServer.url("").uri(), null);
        subject.executeHeadRequest(mockWebServer.url("").uri(), null);

        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Cookie"), isEmptyOrNullString());
    }

    @Test
    public void when204Response_returnsHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204)
                .addHeader("X-1", "Blah1")
                .addHeader("X-2", "Blah2"));

        HttpHeadResult result = subject.executeHeadRequest(mockWebServer.url("").uri(), null);
        assertThat(result.getHeaders().get("X-1"), equalTo("Blah1"));
        assertThat(result.getHeaders().get("X-2"), equalTo("Blah2"));
    }

    @Test
    public void withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\""));
        mockWebServer.enqueue(new MockResponse());

        subject.executeHeadRequest(mockWebServer.url("").uri(), new HttpCredentials("user", "pass"));

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void withCredentials_whenHttps_retriesWithCredentials() throws Exception  {
        startHttpsMockWebServer();

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

    @Test
    public void whenRequestFails_throwsExceptionWithMessage() {
        try {
            subject.executeHeadRequest(new URI("http://localhost:8443"), null);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), not(isEmptyString()));
        }
    }

    private void startHttpsMockWebServer() throws IOException {
        httpsMockWebServer = new MockWebServer();

        if (useRealHttps()) {
            httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        }

        httpsMockWebServer.start(8443);
    }
}