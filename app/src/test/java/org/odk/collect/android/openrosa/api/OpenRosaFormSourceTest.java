package org.odk.collect.android.openrosa.api;

import org.junit.Test;
import org.odk.collect.android.openrosa.HttpGetResult;
import org.odk.collect.android.openrosa.OpenRosaConstants;
import org.odk.collect.android.openrosa.OpenRosaFormSource;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.OpenRosaResponseParser;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.forms.FormSourceException;

import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.net.ssl.SSLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class OpenRosaFormSourceTest {
    private final OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
    private final WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);
    private final OpenRosaResponseParser responseParser = mock(OpenRosaResponseParser.class);

    @Test
    public void fetchFormList_removesTrailingSlashesFromUrl() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com///", httpInterface, webCredentialsUtils, responseParser);

        when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(
                new ByteArrayInputStream(RESPONSE.getBytes()),
                new HashMap<String, String>() {{
                    put(OpenRosaConstants.VERSION_HEADER, "1.0");
                }},
                "", 200
        ));

        formListApi.fetchFormList();
        verify(httpInterface).executeGetRequest(eq(new URI("http://blah.com/formList")), any(), any());
    }

    @Test
    public void fetchFormList_whenThereIsAnUnknownHostException_throwsUnreachableFormApiException() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.Unreachable e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenThereIsAnSSLException_throwsSecurityErrorFormApiException() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(SSLException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.SecurityError e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenThereIsATimeout_throwsFetchError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(SocketTimeoutException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.FetchError e) {
            // pass
        }
    }

    @Test
    public void fetchFormList_whenThereIsA404_throwsUnreachableApiException() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 404));
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.Unreachable e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 500));
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.ServerError e) {
            assertThat(e.getStatusCode(), is(500));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenOpenRosaResponse_whenParserFails_throwsParseError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(
                    new ByteArrayInputStream("<xml></xml>".getBytes()),
                    new HashMap<String, String>() {{
                        put(OpenRosaConstants.VERSION_HEADER, "1.0");
                    }},
                    "hash",
                    200
            ));

            when(responseParser.parseFormList(any())).thenReturn(null);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException.ParseError e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenResponseHasNoOpenRosaHeader_throwsServerNotOpenRosaError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com///", httpInterface, webCredentialsUtils, responseParser);

        when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(
                new ByteArrayInputStream(RESPONSE.getBytes()),
                new HashMap<String, String>(), // No OpenRosa header
                "", 200
        ));

        try {
            formListApi.fetchFormList();
            fail("Expected exception because server is not OpenRosa server.");
        } catch (FormSourceException.ServerNotOpenRosaError e) {
            // pass
        }
    }

    @Test
    public void fetchManifest_whenThereIsAnUnknownHostException_throwsUnreachableFormApiException() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormSourceException.Unreachable e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchManifest_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 503));
            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormSourceException.ServerError e) {
            assertThat(e.getStatusCode(), is(503));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchManifest_whenOpenRosaResponse_whenParserFails_throwsParseError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(
                    new ByteArrayInputStream("<xml></xml>".getBytes()),
                    new HashMap<String, String>() {{
                        put(OpenRosaConstants.VERSION_HEADER, "1.0");
                    }},
                    "hash",
                    200
            ));

            when(responseParser.parseManifest(any())).thenReturn(null);
            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormSourceException.ParseError e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchManifest_whenNotOpenRosaResponse_throwsParseError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(
                    new ByteArrayInputStream("<xml></xml>".getBytes()),
                    new HashMap<>(),
                    "hash",
                    200
            ));

            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormSourceException.ParseError e) {
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchForm_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 500));
            formListApi.fetchForm("http://blah.com/form");
            fail("No exception thrown!");
        } catch (FormSourceException.ServerError e) {
            assertThat(e.getStatusCode(), is(500));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchMediaFile_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", httpInterface, webCredentialsUtils, responseParser);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 500));
            formListApi.fetchMediaFile("http://blah.com/mediaFile");
            fail("No exception thrown!");
        } catch (FormSourceException.ServerError e) {
            assertThat(e.getStatusCode(), is(500));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    private static String join(String... strings) {
        StringBuilder bob = new StringBuilder();
        for (String s : strings) {
            bob.append(s).append('\n');
        }
        return bob.toString();
    }

    private static final String RESPONSE = join(
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
