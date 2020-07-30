package org.odk.collect.android.openrosa.api;

import org.junit.Test;
import org.odk.collect.android.openrosa.HttpGetResult;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.UNKNOWN_HOST;

public class OpenRosaFormListApiTest {

    @Test
    public void fetchFormList_whenThereIsAnUnknownHostException_throwsUnknownHostFormApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormListApi formListApi = new OpenRosaFormListApi("http://blah.com", "/formList", httpInterface, webCredentialsUtils);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchFormList();
            fail("No exception thrown!");
        } catch (FormApiException e) {
            assertThat(e.getType(), is(UNKNOWN_HOST));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchManifest_whenThereIsAnUnknownHostException_throwsUnknownHostFormApiException() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormListApi formListApi = new OpenRosaFormListApi("http://blah.com", "/formList", httpInterface, webCredentialsUtils);

        try {
            when(httpInterface.executeGetRequest(any(), any(), any())).thenThrow(UnknownHostException.class);
            formListApi.fetchManifest("http://blah.com/manifest");
            fail("No exception thrown!");
        } catch (FormApiException e) {
            assertThat(e.getType(), is(UNKNOWN_HOST));
            assertThat(e.getServerUrl(), is("http://blah.com"));
        }
    }

    @Test
    public void fetchFormList_removesTrailingSlashesFromUrl() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        OpenRosaFormListApi formListApi = new OpenRosaFormListApi("http://blah.com///", "/formList", httpInterface, webCredentialsUtils);

        when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(new ByteArrayInputStream(RESPONSE.getBytes()), Collections.emptyMap(), "", 200));
        formListApi.fetchFormList();
        verify(httpInterface).executeGetRequest(eq(new URI("http://blah.com/formList")), any(), any());

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