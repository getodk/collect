package org.odk.collect.android.tasks;

import org.junit.Test;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.test.MockedServerTest;

import java.util.Map;

import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.odk.collect.android.test.TestUtils.assertMatches;

public class DownloadFormListTaskTest extends MockedServerTest {

    @Test
    public void shouldProcessAndReturnAFormList() throws Exception {
        // given
        willRespondWith(RESPONSE);

        // when
        final Map<String, FormDetails> fetched = new DownloadFormListTask().doInBackground();

        // then
        RecordedRequest r = nextRequest();
        assertEquals("GET", r.getMethod());
        assertEquals("/formList", r.getPath());
        assertMatches("Dalvik/.* org.odk.collect.android/.*", r.getHeader("User-Agent"));
        assertEquals("1.0", r.getHeader("X-OpenRosa-Version"));
        assertEquals("gzip", r.getHeader("Accept-Encoding"));

        // and
        assertEquals(2, fetched.size());

        // and
        FormDetails f1 = fetched.get("one");
        assertNull(f1.errorStr);
        assertEquals("The First Form", f1.formName);
        assertEquals("https://example.com/formXml?formId=one", f1.downloadUrl);
        assertNull(f1.manifestUrl);
        assertEquals("one", f1.formID);
        assertNull(f1.formVersion);

        // and
        FormDetails f2 = fetched.get("two");
        assertNull(f2.errorStr);
        assertEquals("The Second Form", f2.formName);
        assertEquals("https://example.com/formXml?formId=two", f2.downloadUrl);
        assertNull(f2.manifestUrl);
        assertEquals("two", f2.formID);
        assertNull(f2.formVersion);
    }

    private static final String RESPONSE = join(
        "HTTP/1.1 200 OK\r",
        "X-OpenRosa-Version: 1.0\r",
        "X-OpenRosa-Accept-Content-Length: 10485760\r",
        "Content-Type: text/xml; charset=utf-8\r",
        "X-Cloud-Trace-Context: cb84da0bfcb4da37910faf33b10ca190;o=1\r",
        "Date: Tue, 18 Apr 2017 15:45:03 GMT\r",
        "Server: Google Frontend\r",
        "Content-Length: 2235\r",
        "Alt-Svc: quic=\":443\"; ma=2592000; v=\"37,36,35\"\r",
        "Connection: close\r",
        "\r",
        "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">",
        "<xform><formID>one</formID>",
        "<name>The First Form</name>",
        "<majorMinorVersion></majorMinorVersion>",
        "<version></version>",
        "<hash>md5:b71c92bec48730119eab982044a8adff</hash>",
        "<downloadUrl>https://example.com/formXml?formId=one</downloadUrl>",
        "</xform>",
        "<xform><formID>two</formID>",
        "<name>The Second Form</name>",
        "<majorMinorVersion></majorMinorVersion>",
        "<version></version>",
        "<hash>md5:4428adffbbec48771c9230119eab9820</hash>",
        "<downloadUrl>https://example.com/formXml?formId=two</downloadUrl>",
        "</xform>",
        "</xforms>");
}
