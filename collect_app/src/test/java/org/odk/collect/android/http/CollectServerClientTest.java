package org.odk.collect.android.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.http.openrosa.HttpGetResult;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.http.stub.StubOpenRosaHttpInterface;
import org.odk.collect.android.http.stub.StubOpenRosaHttpInterfaceError;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CollectServerClientTest {

    private static final String URL_STRING = "http://testurl";

    @Test
    public void getXMLDocument_whenUnsuccessful_returnsResultWithStatusAndErrorMessage() throws Exception {
        OpenRosaHttpInterface httpInterface = mock(OpenRosaHttpInterface.class);
        CollectServerClient collectServerClient = new CollectServerClient(httpInterface, new WebCredentialsUtils());

        when(httpInterface.executeGetRequest(any(), any(), any())).thenReturn(new HttpGetResult(null, new HashMap<>(), "", 500));
        DocumentFetchResult result = collectServerClient.getXmlDocument(URL_STRING);
        assertThat(result.responseCode, equalTo(500));
        assertThat(result.errorMessage, equalTo("getXmlDocument failed while accessing http://testurl with status code: 500"));
    }

    @Test
    public void testGetXMLDocumentErrorResponse() {
        CollectServerClient collectServerClientError = new CollectServerClient(new StubOpenRosaHttpInterfaceError(), new WebCredentialsUtils());

        DocumentFetchResult fetchResult = collectServerClientError.getXmlDocument(URL_STRING);
        assertEquals(fetchResult.errorMessage, "Parsing failed with null while accessing " + URL_STRING);
    }

    @Test
    public void testGetXMLDocument() {
        CollectServerClient collectServerClient = new CollectServerClient(new StubOpenRosaHttpInterface(), new WebCredentialsUtils());

        DocumentFetchResult fetchResult = collectServerClient.getXmlDocument(URL_STRING);
        assertNull(fetchResult.errorMessage);
        assertEquals(fetchResult.responseCode, 0);
        assertTrue(fetchResult.isOpenRosaResponse);
    }

    @Test
    public void testGetPlainTextMimeType() {
        assertEquals(CollectServerClient.getPlainTextMimeType(), "text/plain");
    }
}


