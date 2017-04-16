package org.odk.collect.android.tasks;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.odk.collect.android.tasks.InstanceUploaderTask.Outcome;

import okhttp3.mockwebserver.*;

import static org.junit.Assert.*;

public class InstanceUploaderTaskTest {
    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldUploadASingleInstance() throws Exception {
        // given
        // TODO loads of horrible setup
        server.enqueue(new MockResponse());

        // when
        Outcome o = new InstanceUploaderTask().doInBackground();

        // then
        // TODO assert details of the Outcome
        // TODO assert an appropriate amount of server interaction
        RecordedRequest r = server.takeRequest(1, TimeUnit.MILLISECONDS);
        assertEquals("check what the headers are", r.getHeaders().toString());
    }
}
