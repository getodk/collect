package org.odk.collect.android.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
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
        String fileContent = "<node>content</node>";
        File tempFile = createTempFile(fileContent);
        subject.uploadSubmissionFile(new ArrayList<>(), tempFile, uri, null, 0);

        RecordedRequest request = mockWebServer.takeRequest();
        String[] bodyParts = request.getBody().readUtf8().split("\r\n");
        assertThat(bodyParts[1], containsString("name=\"xml_submission_file\""));
        assertThat(bodyParts[1], containsString("filename=\"" + tempFile.getName() + "\""));
        assertThat(bodyParts[2], containsString("Content-Type: text/xml"));
        assertThat(bodyParts[5], equalTo("<node>content</node>"));
    }

    @Test
    @Ignore
    public void sendsAttachmentsAsPartsOfBody() {
        fail();
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
        File temp = File.createTempFile("tempfile", ".tmp");

        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write(content);
        bw.close();

        return temp;
    }
}
