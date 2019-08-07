package org.odk.collect.android.http;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;
import okio.Buffer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class OpenRosaGetRequestTest {

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
    public void makesAGetRequestToUri() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        subject.executeGetRequest(uri, null, null);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("GET"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void sendsCollectHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("User-Agent"), equalTo(String.format(
                "null %s/%s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME)));
    }

    @Test
    public void sendsOpenRosaHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-OpenRosa-Version"), equalTo("1.0"));
    }

    @Test
    public void sendsAcceptsGzipHeader() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding"), equalTo("gzip"));
    }

    @Test
    public void withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, new HttpCredentials("user", "pass"));

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void withCredentials_whenHttps_retriesWithCredentials() throws Exception {
        startHttpsMockWebServer();

        httpsMockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        httpsMockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(httpsMockWebServer.url("").uri(), null, new HttpCredentials("user", "pass"));

        assertThat(httpsMockWebServer.getRequestCount(), equalTo(2));
        httpsMockWebServer.takeRequest();
        RecordedRequest request = httpsMockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"), equalTo("Basic dXNlcjpwYXNz"));
    }

    @Test
    public void whenLastRequestSetCookies_nextRequestDoesNotSendThem() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Set-Cookie", "blah=blah"));
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Cookie"), isEmptyOrNullString());
    }

    @Test
    public void returnsBodyWithEmptyHash() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
        assertThat(result.getHash(), equalTo(""));
    }

    @Test
    public void whenResponseIsGzipped_returnsBody() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Encoding", "gzip")
                .setBody(new Buffer().write(gzip("I AM BODY"))));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
    }

    @Test
    public void whenContentTypeIsXML_returnsBodyWithMD5Hash() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "text/xml")
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), "text/xml", null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
        assertThat(result.getHash(), equalTo(FileUtils.getMd5Hash(new ByteArrayInputStream("I AM BODY".getBytes()))));
    }

    @Test(expected = Exception.class)
    public void withContentType_whenResponseHasDifferentContentType_throwsException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        subject.executeGetRequest(mockWebServer.url("").uri(), "text/xml", null);
    }

    @Test
    public void withContentType_whenResponseContainsContentType_returnsResult() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), "application/json", null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
    }

    @Test
    public void returnsOpenRosaVersion() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("X-OpenRosa-Version", "1.0"));

        HttpGetResult result1 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result1.isOpenRosaResponse(), equalTo(true));

        mockWebServer.enqueue(new MockResponse());

        HttpGetResult result2 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result2.isOpenRosaResponse(), equalTo(false));
    }

    @Test
    public void whenStatusCodeIsNot200_returnsNullBodyAndStatusCode() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result.getInputStream(), nullValue());
        assertThat(result.getStatusCode(), equalTo(500));
    }

    @Test
    public void whenResponseBodyIsNull_returnsNullBodyAndStatusCode() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        HttpGetResult result1 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result1.getInputStream(), nullValue());
        assertThat(result1.getStatusCode(), equalTo(204));

        mockWebServer.enqueue(new MockResponse().setResponseCode(304));

        HttpGetResult result2 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result2.getInputStream(), nullValue());
        assertThat(result2.getStatusCode(), equalTo(304));
    }

    private void startHttpsMockWebServer() throws IOException {
        httpsMockWebServer = new MockWebServer();

        if (useRealHttps()) {
            httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        }

        httpsMockWebServer.start(8443);
    }

    private static byte[] gzip(String data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream);
        gzipStream.write(data.getBytes());
        gzipStream.close();

        byte[] compressed = outputStream.toByteArray();
        outputStream.close();

        return compressed;
    }
}