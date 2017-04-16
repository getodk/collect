package org.odk.collect.android.tasks;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.odk.collect.android.logic.FormDetails;

import okhttp3.mockwebserver.*;

import static org.junit.Assert.*;
import static org.odk.collect.android.test.MockedServerTestUtils.willRespond;

public class DownloadFormListTaskTest {
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
    public void shouldProcessAndReturnAFormList() throws Exception {
        // given
        willRespond(server,
                "Header-1: value-1",
                "Header-2: value-2",
                "<some><massiveXml/></some>");
        // TODO loads of other horrible setup

        // when
    	Map<String, FormDetails> fetched = new DownloadFormListTask().doInBackground();

        // then
        // TODO assert details of the fetched data

        // and
        // TODO assert an appropriate amount of server interaction
        RecordedRequest r = server.takeRequest(1, TimeUnit.MILLISECONDS);
        assertEquals("check what the headers are", r.getHeaders().toString());
    }
}
