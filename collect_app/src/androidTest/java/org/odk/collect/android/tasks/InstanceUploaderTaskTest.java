package org.odk.collect.android.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.tasks.InstanceUploaderTask.Outcome;

public class InstanceUpploaderTaskTest {
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
    public void shouldUploadASingleInstance() {
        // given
        // TODO loads of horrible setup

        // when
        Outcome o = new InstanceUploaderTask().doInBackground();

        // then
        // TODO assert details of the Outcome
        // TODO assert an appropriate amount of server interaction
    }
}
