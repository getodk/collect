package org.odk.collect.android.http;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class OpenRosaPostRequestTest {

    protected abstract OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper);
    protected abstract Boolean useRealHttps();

    private final MockWebServer mockWebServer = new MockWebServer();
    private MockWebServer httpsMockWebServer;
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        subject = buildSubject(new XmlOrBlahContentTypeMapper());
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
    public void makesAPostRequestToUri() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("POST"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception  {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate."));
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, new HttpCredentials("user", "pass"), 0);

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

        URI uri = httpsMockWebServer.url("").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, new HttpCredentials("user", "pass"), 0);

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

        URI uri = mockWebServer.url("").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Cookie"), isEmptyOrNullString());
    }

    @Test
    public void returnsPostBody() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("blah"));

        URI uri = mockWebServer.url("/blah").uri();
        HttpPostResult response = subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(response.getResponseCode(), equalTo(200));
    }

    @Test
    public void whenThereIsAServerError_returnsPostBody() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("blah"));

        URI uri = mockWebServer.url("/blah").uri();
        HttpPostResult response = subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), equalTo(500));
    }

    @Test
    public void whenRequestFails_throwsExceptionWithMessage() {
        try {
            URI uri = new URI("http://localhost:8443");
            subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);
            fail();
        } catch (Exception e) {
            assertThat(e, isA(Exception.class));
            assertThat(e.getMessage(), not(isEmptyString()));
        }
    }

    @Test
    public void sendsSubmissionFileAsFirstPartOfBody() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        String submissionContent = "<node>content</node>";
        File tempFile = createTempFile(submissionContent);
        subject.uploadSubmissionFile(new ArrayList<>(), tempFile, uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        String[] firstPartLines = splitMultiPart(request).get(0);
        assertThat(firstPartLines[1], containsString("name=\"xml_submission_file\""));
        assertThat(firstPartLines[1], containsString("filename=\"" + tempFile.getName() + "\""));
        assertThat(firstPartLines[2], containsString("Content-Type: text/xml"));
        assertThat(firstPartLines[5], equalTo("<node>content</node>"));
    }

    @Test
    public void sendsAttachmentsAsPartsOfBody() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        File attachment = createTempFile("blah blah blah");
        subject.uploadSubmissionFile(singletonList(attachment), createTempFile("<node>content</node>"), uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        String[] secondPartLines = splitMultiPart(request).get(1);
        assertThat(secondPartLines[1], containsString("name=\"" + attachment.getName() + "\""));
        assertThat(secondPartLines[1], containsString("filename=\"" + attachment.getName() + "\""));
        assertThat(secondPartLines[5], equalTo("blah blah blah"));
    }

    @Test
    public void sendsAttachmentsAsPartsOfBody_withContentType() throws Exception {
        mockWebServer.enqueue(new MockResponse());
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        File xmlAttachment = createTempFile("<node>blah blah blah</node>", ".xml");
        File plainAttachment = createTempFile("blah", ".blah");

        subject.uploadSubmissionFile(singletonList(xmlAttachment), createTempFile("<node>content</node>"), uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        List<String[]> parts = splitMultiPart(request);
        assertThat(parts.get(1)[2], containsString("Content-Type: text/xml"));

        subject.uploadSubmissionFile(singletonList(plainAttachment), createTempFile("<node>content</node>"), uri, null, 0);

        request = mockWebServer.takeRequest();
        parts = splitMultiPart(request);
        assertThat(parts.get(1)[2], containsString("Content-Type: text/blah"));
    }

    @Test
    @Ignore
    public void whenRequestIsLargerThanMaxContentLength_sendsTwoRequests() {
        fail();
    }

    @Test
    @Ignore
    public void whenRequestIsLargerThanMaxContentLength_andFirstRequestIs500_returnsErrorResult() {
        fail();
    }

    @Test
    @Ignore
    public void whenRequestIsLargerThanMaxContentLength_andSecondRequestIs500_returnsErrorResult() {
        fail();
    }

    @Test
    @Ignore
    public void whenRequestIsLargerThanMaxContentLength_andFirstRequestFails_throwsExceptionWithMessage() {
        fail();
    }

    @Test
    @Ignore
    public void whenRequestIsLargerThanMaxContentLength_andSecondRequestFails_throwsExceptionWithMessage() {
        fail();
    }

    private void startHttpsMockWebServer() throws IOException {
        httpsMockWebServer = new MockWebServer();

        if (useRealHttps()) {
            httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        }

        httpsMockWebServer.start(8443);
    }

    private File createTempFile(String content) throws Exception {
        return createTempFile(content, ".tmp");
    }

    private File createTempFile(String content, String extension) throws Exception {
        File temp = File.createTempFile("tempfile", extension);

        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write(content);
        bw.close();

        return temp;
    }

    private List<String[]> splitMultiPart(RecordedRequest request) {
        String body = request.getBody().readUtf8();
        String boundary = body.split("\r\n")[0];
        String[] split = body.split(boundary);
        String[] stringParts = Arrays.copyOfRange(split, 1, split.length - 1);
        return Arrays.stream(stringParts).map(part -> part.split("\r\n")).collect(Collectors.toList());
    }

    private class XmlOrBlahContentTypeMapper implements OpenRosaHttpInterface.FileToContentTypeMapper {

        @NonNull
        @Override
        public String map(String fileName) {
            if (fileName.endsWith(".xml")) {
                return "text/xml";
            } else {
                return "text/blah";
            }
        }
    }
}
