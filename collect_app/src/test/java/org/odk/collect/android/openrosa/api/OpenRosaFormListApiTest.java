package org.odk.collect.android.openrosa.api;

import org.junit.Test;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
        }
    }
}