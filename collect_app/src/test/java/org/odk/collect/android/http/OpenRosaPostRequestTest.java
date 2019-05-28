package org.odk.collect.android.http;

import com.google.common.collect.Collections2;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static java.util.Arrays.asList;
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

    protected abstract OpenRosaHttpInterface buildSubject();

    private final MockWebServer mockWebServer = new MockWebServer();
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        mockWebServer.start();
        subject = buildSubject();
    }

    @After
    public void teardown() throws Exception {
        mockWebServer.shutdown();
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
        assertThat(secondPartLines[2], containsString("Content-Type: application/octet-stream"));
        assertThat(secondPartLines[5], equalTo("blah blah blah"));
    }

    @Test
    public void whenAttachmentHasRecognizedExtension_sendsWithContentType() throws Exception {
        mockWebServer.enqueue(new MockResponse());

        URI uri = mockWebServer.url("/blah").uri();
        File attachment = createTempFile("<node>blah blah blah</node>", ".xml");
        subject.uploadSubmissionFile(singletonList(attachment), createTempFile("<node>content</node>"), uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        String[] secondPartLines = splitMultiPart(request).get(1);
        assertThat(secondPartLines[2], containsString("Content-Type: text/xml"));
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
}
