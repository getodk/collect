package org.odk.collect.android.tasks;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void shouldProcessAndReturnAFormList() {
        // given
        // TODO loads of horrible setup

        // when
    	Map<String, FormDetails> fetched = new DownloadFormListTask().doInBackground();

        // then
        // TODO assert details of the fetched data
        // TODO assert an appropriate amount of server interaction
    }
}
