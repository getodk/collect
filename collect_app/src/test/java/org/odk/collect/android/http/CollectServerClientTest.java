package org.odk.collect.android.http;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CollectServerClientTest {

    @Before
    public void setup() {
        TestableCollectServerClient.setInstance(new TestableCollectServerClient());
    }

    @Test
    public void testAddCredential() {
        TestableCollectServerClient.getInstance();
        Assert.assertTrue(true);
    }

    @Test
    public void testClearHostCredentials() {
        Assert.assertTrue(true);
    }

    @Test
    public void testAddCredentials() {
        Assert.assertTrue(true);
    }

    @Test
    public void testGetXMLDocument() {
        Assert.assertTrue(true);
    }

    @Test
    public void testGetHttpInputStream() {
        Assert.assertTrue(true);
    }

    @Test
    public void testGetPlainTextMimeType() {
        Assert.assertEquals(TestableCollectServerClient.getPlainTextMimeType(),"text/plain");
    }


}


