package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CollectServerClientTest {

    @Inject
    TestableCollectServerClient collectServerClient;

    @Before
    public void setup() {
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        testComponent.inject(this);
    }

    @Test
    public void testGetXMLDocumentErrorResponse() {
        collectServerClient.setGetHttpShouldReturnNull(true);
        DocumentFetchResult fetchResult = collectServerClient.getXmlDocument("http://testurl");
        assertEquals(fetchResult.errorMessage, "Parsing failed with null while accessing http://testurl");
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
        assertEquals(TestableCollectServerClient.getPlainTextMimeType(), "text/plain");
    }
}


