package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.http.mock.MockHttpClientConnection;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.net.URL;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class CollectServerClientTest {

    @Inject
    CollectServerClient collectServerClient;

    @Before
    public void setup() {
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);
    }

    @Test
    public void testGetXMLDocumentErrorResponse() {
        final String urlString = "http://testurl";

        MockHttpClientConnection clientConnection = mock(MockHttpClientConnection.class);

        try {
            URL url = new URL(urlString);
            doReturn(null).when(clientConnection).get(url.toURI(), CollectServerClient.HTTP_CONTENT_TYPE_TEXT_XML, null);
        } catch (Exception e) {
            fail("Exception Thrown mocking MockHttpClientConnection.get()");
        }

        CollectServerClient collectServer = new CollectServerClient(clientConnection);
        DocumentFetchResult fetchResult = collectServer.getXmlDocument(urlString);
        assertEquals(fetchResult.errorMessage, "Parsing failed with null while accessing " + urlString);
    }

    @Test
    public void testGetXMLDocument() {
        DocumentFetchResult fetchResult = collectServerClient.getXmlDocument("http://testurl");
        assertNull(fetchResult.errorMessage);
        assertEquals(fetchResult.responseCode, 0);
        assertTrue(fetchResult.isOpenRosaResponse);
    }

    @Test
    public void testGetPlainTextMimeType() {
        assertEquals(CollectServerClient.getPlainTextMimeType(), "text/plain");
    }
}


