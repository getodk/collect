package org.odk.collect.android.http;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.internal.TlsUtil;
import okio.Buffer;

import static java.util.Arrays.asList;
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
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/blah").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod(), equalTo("POST"));
        assertThat(request.getRequestUrl().uri(), equalTo(uri));
    }

    @Test
    public void withCredentials_whenHttp_doesNotRetryWithCredentials() throws Exception {
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
        mockWebServer.enqueue(new MockResponse().setResponseCode(201)
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
    public void returnsPostResult() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("I AM BODY"));

        URI uri = mockWebServer.url("/blah").uri();
        HttpPostResult response = subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(response.getResponseCode(), equalTo(200));
        assertThat(response.getHttpResponse(), equalTo("I AM BODY"));
    }

    @Test
    public void whenResponseIsGzipped_returnsBody() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Encoding", "gzip")
                .setBody(new Buffer().write(gzip("I AM BODY"))));

        URI uri = mockWebServer.url("/blah").uri();
        HttpPostResult response = subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);

        assertThat(response.getHttpResponse(), equalTo("I AM BODY"));
    }

    @Test(expected = Exception.class)
    public void whenResponseIs204_throwsException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        URI uri = mockWebServer.url("/blah").uri();
        subject.uploadSubmissionFile(new ArrayList<>(), File.createTempFile("blah", "blah"), uri, null, 0);
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
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

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
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/blah").uri();
        File attachment1 = createTempFile("blah blah blah");
        File attachment2 = createTempFile("blah2 blah2 blah2");
        subject.uploadSubmissionFile(asList(attachment1, attachment2), createTempFile("<node>content</node>"), uri, null, 1024);

        RecordedRequest request = mockWebServer.takeRequest();
        List<String[]> parts = splitMultiPart(request);

        String[] secondPartLines = parts.get(1);
        assertThat(secondPartLines[1], containsString("name=\"" + attachment1.getName() + "\""));
        assertThat(secondPartLines[1], containsString("filename=\"" + attachment1.getName() + "\""));
        assertThat(secondPartLines[5], equalTo("blah blah blah"));

        String[] thirdPartLines = parts.get(2);
        assertThat(thirdPartLines[1], containsString("name=\"" + attachment2.getName() + "\""));
        assertThat(thirdPartLines[1], containsString("filename=\"" + attachment2.getName() + "\""));
        assertThat(thirdPartLines[5], equalTo("blah2 blah2 blah2"));
    }

    @Test
    public void sendsAttachmentsAsPartsOfBody_withContentType() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/blah").uri();
        File xmlAttachment = createTempFile("<node>blah blah blah</node>", ".xml");
        File plainAttachment = createTempFile("blah", ".blah");

        subject.uploadSubmissionFile(asList(xmlAttachment, plainAttachment), createTempFile("<node>content</node>"), uri, null, 1024);

        RecordedRequest request = mockWebServer.takeRequest();
        List<String[]> parts = splitMultiPart(request);
        assertThat(parts.get(1)[2], containsString("Content-Type: text/xml"));
        assertThat(parts.get(2)[2], containsString("Content-Type: text/blah"));
    }

    @Test
    public void whenMoreThanOneAttachment_andRequestIsLargerThanMaxContentLength_sendsTwoRequests() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/blah").uri();
        File attachment1 = createTempFile("blah blah blah");
        File attachment2 = createTempFile("blah2 blah2 blah2");
        subject.uploadSubmissionFile(asList(attachment1, attachment2), createTempFile("<node>content</node>"), uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        List<String[]> parts = splitMultiPart(request);

        assertThat(parts.size(), equalTo(3));

        String[] secondPartLines = parts.get(1);
        assertThat(secondPartLines[1], containsString("name=\"" + attachment1.getName() + "\""));
        assertThat(secondPartLines[1], containsString("filename=\"" + attachment1.getName() + "\""));
        assertThat(secondPartLines[5], equalTo("blah blah blah"));

        String[] thirdPartLines = parts.get(2);
        assertThat(thirdPartLines[1], containsString("name=\"*isIncomplete*\""));

        request = mockWebServer.takeRequest();
        parts = splitMultiPart(request);

        assertThat(parts.size(), equalTo(2));

        secondPartLines = parts.get(1);
        assertThat(secondPartLines[1], containsString("name=\"" + attachment2.getName() + "\""));
        assertThat(secondPartLines[1], containsString("filename=\"" + attachment2.getName() + "\""));
        assertThat(secondPartLines[5], equalTo("blah2 blah2 blah2"));
    }

    @Test
    public void whenMoreThanOneAttachment_andRequestIsLargerThanMaxContentLength__andFirstRequestIs500_returnsErrorResult() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        URI uri = mockWebServer.url("/blah").uri();
        File attachment1 = createTempFile("blah blah blah");
        File attachment2 = createTempFile("blah2 blah2 blah2");
        HttpPostResult response = subject.uploadSubmissionFile(asList(attachment1, attachment2), createTempFile("<node>content</node>"), uri, null, 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), equalTo(500));
    }

    @Test
    public void whenMoreThanOneAttachment_andRequestIsLargerThanMaxContentLength_andSecondRequestIs500_returnsErrorResult() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        URI uri = mockWebServer.url("/blah").uri();
        File attachment1 = createTempFile("blah blah blah");
        File attachment2 = createTempFile("blah2 blah2 blah2");
        HttpPostResult response = subject.uploadSubmissionFile(asList(attachment1, attachment2), createTempFile("<node>content</node>"), uri, null, 0);

        assertThat(mockWebServer.getRequestCount(), equalTo(2));
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), equalTo(500));
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

    private static byte[] gzip(String data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream);
        gzipStream.write(data.getBytes());
        gzipStream.close();

        byte[] compressed = outputStream.toByteArray();
        outputStream.close();

        return compressed;
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
