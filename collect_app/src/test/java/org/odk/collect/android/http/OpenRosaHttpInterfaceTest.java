package org.odk.collect.android.http;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.Charset;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class OpenRosaHttpInterfaceTest {

    protected abstract OpenRosaHttpInterface buildSubject();

    private final MockWebServer mockWebServer = new MockWebServer();
    private final MockWebServer httpsMockWebServer = new MockWebServer();
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        httpsMockWebServer.start(8443);

        subject = buildSubject();
    }

    @After
    public void teardown() throws Exception {
        mockWebServer.shutdown();
        httpsMockWebServer.shutdown();
    }

    @Test
    public void executeGetRequest_makesAGetRequestToUri() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        subject.executeGetRequest(uri, null, null);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("GET"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void executeGetRequest_sendsCollectHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("User-Agent"), equalTo(String.format(
                "null %s/%s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME)));
    }

    @Test
    public void executeGetRequest_sendsOpenRosaHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-OpenRosa-Version"), equalTo("1.0"));
    }

    @Test
    public void executeGetRequest_sendsAcceptsGzipHeader() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding"), equalTo("gzip"));
    }

    @Test
    @Ignore("OkHttpConnection not fooled into https behaviour by port 8443 - scheme needs to be https")
    public void executeGetRequest_withCredentials_whenHttps_retriesWithCredentials() throws Exception  {
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
    public void executeGetRequest_withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception  {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        subject.executeGetRequest(mockWebServer.url("").uri(), null, new HttpCredentials("user", "pass"));

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void executeGetRequest_returnsBodyWithEmptyHash() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
        assertThat(result.getHash(), equalTo(""));
    }

    @Test
    public void executeGetRequest_whenContentTypeIsXML_returnsBodyWithMD5Hash() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "text/xml")
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), "text/xml", null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
        assertThat(result.getHash(), equalTo(FileUtils.getMd5Hash(new ByteArrayInputStream("I AM BODY".getBytes()))));
    }

    @Test(expected = Exception.class)
    public void executeGetRequest_withContentType_whenResponseHasDifferentContentType_throwsException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        subject.executeGetRequest(mockWebServer.url("").uri(), "text/xml", null);
    }

    @Test(expected = Exception.class)
    @Ignore("OkHttp doesn't recognize bogus content types")
    public void executeGetRequest_withBogusContentType_whenResponseHasDifferentBogusContentType_throwsException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "bad stuff"));

        subject.executeGetRequest(mockWebServer.url("").uri(), "good stuff", null);
    }

    @Test
    public void executeGetRequest_withContentType_whenResponseContainsContentType_returnsResult() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("I AM BODY"));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), "application/json", null);
        assertThat(IOUtils.toString(result.getInputStream(), Charset.defaultCharset()), equalTo("I AM BODY"));
    }

    @Test
    public void executeGetRequest_returnsOpenRosaVersion() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("X-OpenRosa-Version", "1.0"));

        HttpGetResult result1 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result1.isOpenRosaResponse(), equalTo(true));

        mockWebServer.enqueue(new MockResponse());

        HttpGetResult result2 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result2.isOpenRosaResponse(), equalTo(false));
    }

    @Test
    public void executeGetRequest_whenStatusCodeIsNot200_returnsNullBodyAndStatusCode() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        HttpGetResult result = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result.getInputStream(), nullValue());
        assertThat(result.getStatusCode(), equalTo(500));
    }

    /**
     * The original {@link HttpClientConnection} implementation performs a null check on the response
     * entity (body) and then throws exception if it is null. However, this should never actually happen
     * as the 204/304 response that would cause a null body will return an empty result object with
     * the status code before that check happen.
     */
    @Test
    public void executeGetRequest_whenResponseBodyIsNull_returnsNullBodyAndStatusCode() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        HttpGetResult result1 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result1.getInputStream(), nullValue());
        assertThat(result1.getStatusCode(), equalTo(204));

        mockWebServer.enqueue(new MockResponse().setResponseCode(304));

        HttpGetResult result2 = subject.executeGetRequest(mockWebServer.url("").uri(), null, null);
        assertThat(result2.getInputStream(), nullValue());
        assertThat(result2.getStatusCode(), equalTo(304));
    }
}