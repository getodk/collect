package org.odk.collect.android.openrosa.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.openrosa.HttpGetResult;
import org.odk.collect.android.openrosa.OpenRosaFormSource;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
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
import static org.odk.collect.android.forms.FormSourceException.Type.SECURITY_ERROR;
import static org.odk.collect.android.forms.FormSourceException.Type.SERVER_ERROR;
import static org.odk.collect.android.forms.FormSourceException.Type.UNREACHABLE;

@RunWith(MockitoJUnitRunner.class)
public class OpenRosaFormSourceTest {

    @Mock
    Analytics analytics;

    @Test
    public void fetchFormList_removesTrailingSlashesFromUrl() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com///", "/formList", httpInterface, webCredentialsUtils, analytics);

        when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(new ByteArrayInputStream(RESPONSE.getBytes()), Collections.emptyMap(), "", 200));
        formListApi.fetchFormList();
        verify(httpInterface).executeGetRequest(eq(new URI("http://blah.com/formList")), any(), any());

    }

    @Test
    public void fetchFormList_whenThereIsAnUnknownHostException_throwsUnreachableFormApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(UNREACHABLE));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenThereIsAnSSLException_throwsSecurityErrorFormApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(SSLException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(SECURITY_ERROR));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_whenThereIsA404_throwsUnreachableApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 404));
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(UNREACHABLE));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchManifest_whenThereIsAnUnknownHostException_throwsUnreachableFormApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(UNREACHABLE));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchForm_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 500));
            formListApi.fetchForm("http://blah.com/form");
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(SERVER_ERROR));
        }
    }

    @Test
    public void fetchMediaFile_whenThereIsAServerError_throwsServerError() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormSource formListApi = new OpenRosaFormSource("http://blah.com", "/formList", httpInterface, webCredentialsUtils, analytics);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "hash", 500));
            formListApi.fetchMediaFile("http://blah.com/mediaFile");
            fail("No exception thrown!");
        } catch (FormSourceException e) {
            assertThat(e.getType(), is(SERVER_ERROR));
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
