package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.http.openrosa.OpenRosaServerClient;
import org.odk.collect.android.http.openrosa.OpenRosaServerClientFactory;
import org.odk.collect.android.utilities.Clock;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class OpenRosaServerClientFactoryTest {

    protected abstract OpenRosaServerClientFactory buildSubject(Clock clock);

    protected abstract Boolean useRealHttps();

    private final MockWebServer mockWebServer = new MockWebServer();
    private final Clock mockClock = mock(Clock.class);

    private MockWebServer httpsMockWebServer;
    private OpenRosaServerClientFactory subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        when(mockClock.getCurrentTime()).thenReturn(new Date());

        subject = buildSubject(mockClock);
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
    public void sendsOpenRosaHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("http", "Android", null);
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-OpenRosa-Version"), equalTo("1.0"));
    }

    @Test
    public void sendsDateHeader() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        Date currentTime = new Date();
        when(mockClock.getCurrentTime()).thenReturn(currentTime);

        OpenRosaServerClient client = subject.create("http", "Android", null);
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());

        RecordedRequest request = mockWebServer.takeRequest();

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss zz", Locale.US);
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertThat(request.getHeader("Date"), equalTo(dateFormatGmt.format(currentTime)));
    }

    @Test
    public void sendsAcceptsGzipHeader() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("http", "Android", null);
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding"), equalTo("gzip"));
    }
    
    @Test
    public void withCredentials_whenBasicChallengeReceived_doesNotRetryWithCredentials() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("http", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void withCredentials_whenBasicChallengeReceived_whenHttps_retriesWithCredentials() throws Exception {
        startHttpsMockWebServer();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("https", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("")).build());

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(2));
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }

    @Test
    public void withCredentials_whenDigestChallengeReceived_retriesWithCredentials() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Digest realm=\"ODK Aggregate\", qop=\"auth\", nonce=\"MTU2NTA4MjEzODI4OTpmMjc4MDM5N2YxZTJiNDRiNjNiYTBiMThiOWQ4ZTlkMg==\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("http", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());

        assertThat(mockWebServer.getRequestCount(), equalTo(2));
        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), startsWith("Digest"));
    }

    @Test
    public void withCredentials_whenDigestChallengeReceived_whenHttps_retriesWithCredentials() throws Exception {
        startHttpsMockWebServer();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Digest realm=\"ODK Aggregate\", qop=\"auth\", nonce=\"MTU2NTA4MjEzODI4OTpmMjc4MDM5N2YxZTJiNDRiNjNiYTBiMThiOWQ4ZTlkMg==\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("https", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("")).build());

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(2));
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), startsWith("Digest"));
    }

    @Test
    public void withCredentials_onceBasicChallenged_whenHttps_proactivelySendsCredentials() throws Exception {
        startHttpsMockWebServer();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());
        httpsMockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("https", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("")).build());
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("/different")).build());

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(3));
        httpsMockWebServer.takeRequest();
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }

    @Test
    public void withCredentials_onceDigestChallenged_proactivelySendsCredentials() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Digest realm=\"ODK Aggregate\", qop=\"auth\", nonce=\"MTU2NTA4MjEzODI4OTpmMjc4MDM5N2YxZTJiNDRiNjNiYTBiMThiOWQ4ZTlkMg==\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());
        mockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("http", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(mockWebServer.url("")).build());
        client.makeRequest(new Request.Builder().url(mockWebServer.url("/different")).build());

        assertThat(mockWebServer.getRequestCount(), equalTo(3));
        mockWebServer.takeRequest();
        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), startsWith("Digest"));
    }

    @Test
    public void withCredentials_onceDigestChallenged_whenHttps_proactivelySendsCredentials() throws Exception {
        startHttpsMockWebServer();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Digest realm=\"ODK Aggregate\", qop=\"auth\", nonce=\"MTU2NTA4MjEzODI4OTpmMjc4MDM5N2YxZTJiNDRiNjNiYTBiMThiOWQ4ZTlkMg==\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());
        httpsMockWebServer.enqueue(new MockResponse());

        OpenRosaServerClient client = subject.create("https", "Android", new HttpCredentials("user", "pass"));
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("")).build());
        client.makeRequest(new Request.Builder().url(httpsMockWebServer.url("/different")).build());

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(3));
        httpsMockWebServer.takeRequest();
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), startsWith("Digest"));
    }

    private void startHttpsMockWebServer() throws IOException {
        httpsMockWebServer = new MockWebServer();

        if (useRealHttps()) {
            httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        }

        httpsMockWebServer.start(8443);
    }
}
