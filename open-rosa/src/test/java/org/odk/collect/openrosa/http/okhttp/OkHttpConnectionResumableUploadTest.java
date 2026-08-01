package org.odk.collect.openrosa.http.okhttp;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.openrosa.http.HttpPostResult;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.openrosa.http.SubmissionUploadProgressTracker;
import org.odk.collect.openrosa.support.MockWebServerRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Covers resuming a chunked upload: with every attachment forced into its own chunk (contentLength
 * 0), the tracker controls which chunk the upload starts from and is told about each chunk the
 * server accepts.
 */
public class OkHttpConnectionResumableUploadTest {

    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule();

    private MockWebServer mockWebServer;
    private OpenRosaHttpInterface subject;

    @Before
    public void setup() throws Exception {
        subject = new OkHttpConnection(null, fileName -> "application/octet-stream", "Test Agent");
        mockWebServer = mockWebServerRule.start();
    }

    @Test
    public void resumingFromAChunk_skipsAlreadyUploadedChunks_andReportsProgress() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/submission").uri();
        RecordingTracker tracker = new RecordingTracker(1); // resume from the 2nd chunk

        subject.uploadSubmissionAndFiles(
                createTempFile("<x/>"),
                asList(createTempFile("AAA"), createTempFile("BBB"), createTempFile("CCC")),
                uri, null, 0, tracker
        );

        // Only chunks 1 and 2 are posted; chunk 0 is treated as already uploaded and skipped.
        assertThat(mockWebServer.getRequestCount(), equalTo(2));
        assertThat(tracker.uploaded, contains(1, 2));

        // The first request actually sent is chunk 1 ("BBB"), and chunk 0 ("AAA") is never sent.
        String firstBody = mockWebServer.takeRequest().getBody().readUtf8();
        assertThat(firstBody, containsString("BBB"));
        assertThat(firstBody, not(containsString("AAA")));
        // Every chunk, including a resumed one, carries the submission XML (as in the original upload).
        assertThat(firstBody, containsString("xml_submission_file"));
    }

    @Test
    public void resumingBeyondTheLastChunk_reSendsOnlyTheFinalChunk() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/submission").uri();
        RecordingTracker tracker = new RecordingTracker(99); // past the end -> clamp to the final chunk

        subject.uploadSubmissionAndFiles(
                createTempFile("<x/>"),
                asList(createTempFile("AAA"), createTempFile("BBB"), createTempFile("CCC")),
                uri, null, 0, tracker
        );

        assertThat(mockWebServer.getRequestCount(), equalTo(1));
        assertThat(tracker.uploaded, contains(2));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), containsString("CCC"));
    }

    @Test
    public void resumingFromZero_uploadsEveryChunk_andRecordsEachOne() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));

        URI uri = mockWebServer.url("/submission").uri();
        RecordingTracker tracker = new RecordingTracker(0);

        subject.uploadSubmissionAndFiles(
                createTempFile("<x/>"),
                asList(createTempFile("AAA"), createTempFile("BBB"), createTempFile("CCC")),
                uri, null, 0, tracker
        );

        assertThat(mockWebServer.getRequestCount(), equalTo(3));
        assertThat(tracker.uploaded, contains(0, 1, 2));
    }

    @Test
    public void whenAChunkFails_stopsAndDoesNotRecordThatChunkAsUploaded() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201)); // chunk 0 accepted
        mockWebServer.enqueue(new MockResponse().setResponseCode(500)); // chunk 1 fails

        URI uri = mockWebServer.url("/submission").uri();
        RecordingTracker tracker = new RecordingTracker(0);

        HttpPostResult result = subject.uploadSubmissionAndFiles(
                createTempFile("<x/>"),
                asList(createTempFile("AAA"), createTempFile("BBB"), createTempFile("CCC")),
                uri, null, 0, tracker
        );

        assertThat(result.getResponseCode(), equalTo(500));
        assertThat(mockWebServer.getRequestCount(), equalTo(2)); // chunk 2 was never attempted
        assertThat(tracker.uploaded, contains(0)); // only chunk 0 recorded; the failed chunk 1 is not
    }

    private File createTempFile(String content) throws Exception {
        File temp = File.createTempFile("tempfile", ".tmp");
        temp.deleteOnExit();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            bw.write(content);
        }
        return temp;
    }

    private static class RecordingTracker implements SubmissionUploadProgressTracker {

        private final int resumeFrom;
        private final List<Integer> uploaded = new ArrayList<>();

        RecordingTracker(int resumeFrom) {
            this.resumeFrom = resumeFrom;
        }

        @Override
        public int getResumeFromChunkIndex() {
            return resumeFrom;
        }

        @Override
        public void onChunkUploaded(int chunkIndex) {
            uploaded.add(chunkIndex);
        }
    }
}
