package org.odk.collect.android.tasks;

import android.net.Uri;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.InstanceUploader.Outcome;
import org.odk.collect.android.test.MockedServerTest;

import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.odk.collect.android.test.TestUtils.assertMatches;
import static org.odk.collect.android.test.TestUtils.cleanUpTempFiles;
import static org.odk.collect.android.test.TestUtils.createTempFile;
import static org.odk.collect.android.test.TestUtils.resetInstancesContentProvider;

public class InstanceServerUploaderTest extends MockedServerTest {
    private InstancesDao dao;

    @Before
    public void setUp() throws Exception {
        resetInstancesContentProvider();
        dao = new InstancesDao();
    }

    @After
    public void tearDown() throws Exception {
        cleanUpTempFiles();
        resetInstancesContentProvider();
    }

    @Test
    public void shouldUploadAnInstance() throws Exception {
        // given
        Long id = createStoredInstance();
        willRespondWith(headResponse(), postResponse());

        // when
        Outcome o = new InstanceServerUploader().doInBackground(id);

        // then
        assertNull(o.authRequestingServer);
        assertEquals(1, o.messagesByInstanceId.size());
        assertEquals("success", o.messagesByInstanceId.get(id.toString()));

        // and
        HEAD: {
            RecordedRequest r = nextRequest();
            assertEquals("HEAD", r.getMethod());
            assertMatches("/submission\\?deviceID=\\w+%3A\\w+", r.getPath());
            assertMatches("Dalvik/.* org.odk.collect.android/.*", r.getHeader("User-Agent"));
            assertEquals("1.0", r.getHeader("X-OpenRosa-Version"));
            assertEquals("gzip,deflate", r.getHeader("Accept-Encoding"));
        }

        // and
        POST: {
            RecordedRequest r = nextRequest();
            assertEquals("POST", r.getMethod());
            assertMatches("/submission\\?deviceID=\\w+%3A\\w+", r.getPath());
            assertMatches("Dalvik/.* org.odk.collect.android/.*", r.getHeader("User-Agent"));
            assertEquals("1.0", r.getHeader("X-OpenRosa-Version"));
            assertEquals("gzip,deflate", r.getHeader("Accept-Encoding"));
            assertMatches("multipart/form-data; boundary=.*", r.getHeader("Content-Type"));
            assertMatches(join(
                            "--.*\r",
                            "Content-Disposition: form-data; name=\"xml_submission_file\"; filename=\"tst.*\\.tmp\"\r",
                            "Content-Type: text/xml; charset=ISO-8859-1\r",
                            "Content-Transfer-Encoding: binary\r",
                            "\r",
                            "<form-content-here/>\r",
                            "--.*--\r"),
                    r.getBody().readUtf8());
        }
    }

    private long createStoredInstance() throws Exception {
        File xml = createTempFile("<form-content-here/>");

        Instance i = new Instance.Builder()
                .displayName("Test Form")
                .instanceFilePath(xml.getAbsolutePath())
                .jrFormId("test_form")
                .status(InstanceProviderAPI.STATUS_COMPLETE)
                .lastStatusChangeDate(123L)
                .displaySubtext("A form used in testing")
                .build();

        Uri contentUri = dao.saveInstance(dao.getValuesFromInstanceObject(i));
        return Long.parseLong(contentUri.toString().substring(InstanceProviderAPI.InstanceColumns.CONTENT_URI.toString().length() + 1));
    }

    private String hostAndPort() {
        return String.format("%s:%s", server.getHostName(), server.getPort());
    }

    private String headResponse() {
        return join(
            "HTTP/1.1 204 No Content\r",
            "X-OpenRosa-Version: 1.0\r",
            "X-OpenRosa-Accept-Content-Length: 10485760\r",
            "Location: http://" + hostAndPort() + "/submission\r",
            "X-Cloud-Trace-Context: 2813267fc382586b60b1d7d494c53d6e;o=1\r",
            "Date: Wed, 19 Apr 2017 22:11:03 GMT\r",
            "Content-Type: text/html\r",
            "Server: Google Frontend\r",
            "Alt-Svc: quic=\":443\"; ma=2592000; v=\"37,36,35\"\r",
            "Connection: close\r",
            "\r");
    }

    private String postResponse() {
        return join(
            "HTTP/1.1 201 Created\r",
            "Location: http://" + hostAndPort() + "/submission\r",
            "X-OpenRosa-Version: 1.0\r",
            "X-OpenRosa-Accept-Content-Length: 10485760\r",
            "Content-Type: text/xml; charset=UTF-8\r",
            "X-Cloud-Trace-Context: d7e2d1c98f475e7fd912545f6cfac4e2\r",
            "Date: Wed, 19 Apr 2017 22:11:05 GMT\r",
            "Server: Google Frontend\r",
            "Content-Length: 373\r",
            "Alt-Svc: quic=\":443\"; ma=2592000; v=\"37,36,35\"\r",
            "Connection: close\r",
            "\r",
            "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\"><message>success</message><submissionMetadata xmlns=\"http://www.opendatakit.org/xforms\" id=\"basic\" instanceID=\"uuid:5167d1cf-8dcb-4fa6-b7f7-ee5dd0265ab5\" submissionDate=\"2017-04-19T22:11:04.181Z\" isComplete=\"true\" markedAsCompleteDate=\"2017-04-19T22:11:04.181Z\"/></OpenRosaResponse>\r",
            "\r");
    }
}
