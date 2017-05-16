package org.odk.collect.android.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebHelper;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.tasks.DownloadFormListTask.DL_AUTH_REQUIRED;
import static org.odk.collect.android.tasks.DownloadFormListTask.DL_ERROR_MSG;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class DownloadFormListTaskTest {

    @Mock
    WebHelper web;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private DownloadFormListTask initDownloadFormListTask(DocumentFetchResult documentFetchResult) {
        when(web.getXmlDocument(anyString(), any(HttpContext.class), any(HttpClient.class)))
                .thenReturn(documentFetchResult);

        return new DownloadFormListTask("fake_url", web);
    }

    @Test
    public void testDoInBackgroundWhenServerReturns401ResponseCode() {

        // this simple class is easier to mock manually without mockito
        final String errorMsg = "ERROR";
        DocumentFetchResult documentFetchResult = new DocumentFetchResult(errorMsg, 401);
        DownloadFormListTask downloadFormListTask = initDownloadFormListTask(documentFetchResult);

        HashMap<String, FormDetails> formList = downloadFormListTask.doInBackground();

        assertEquals(errorMsg, formList.get(DL_AUTH_REQUIRED).errorStr);
        assertFalse(formList.containsKey(DL_ERROR_MSG));
    }

    @Test
    public void testDoInBackgroundWhenServerReturnsNon401ResponseCode() {

        // this simple class is easier to mock manually without mockito
        final String errorMsg = "NOT_FOUND";
        DocumentFetchResult documentFetchResult = new DocumentFetchResult(errorMsg, 404);
        DownloadFormListTask downloadFormListTask = initDownloadFormListTask(documentFetchResult);

        HashMap<String, FormDetails> formList = downloadFormListTask.doInBackground();

        assertFalse(formList.containsKey(DL_AUTH_REQUIRED));
        assertEquals(errorMsg, formList.get(DL_ERROR_MSG).errorStr);
    }

    @Test
    public void testDoInBackgroundWithIncorrectRootElementError() {

        Element rootElement = Mockito.mock(Element.class);
        when(rootElement.getName()).thenReturn("yforms");

        Document document = Mockito.mock(Document.class);
        when(document.getRootElement()).thenReturn(rootElement);

        DocumentFetchResult documentFetchResult = new DocumentFetchResult(document, true);
        DownloadFormListTask downloadFormListTask = initDownloadFormListTask(documentFetchResult);

        HashMap<String, FormDetails> formList = downloadFormListTask.doInBackground();

        String error = "root element is not <xforms> : " + rootElement.getName();
        String expectedErrorStr = Collect.getInstance().getString(R.string.parse_openrosa_formlist_failed, error);

        assertEquals(expectedErrorStr, formList.get(DL_ERROR_MSG).errorStr);
    }

    @Test
    public void testDoInBackgroundWithIncorrectRootElementNamespaceError() {

        Element rootElement = Mockito.mock(Element.class);
        when(rootElement.getName()).thenReturn("xforms");
        when(rootElement.getNamespace()).thenReturn("wrong namespace");

        Document document = Mockito.mock(Document.class);
        when(document.getRootElement()).thenReturn(rootElement);

        DocumentFetchResult documentFetchResult = new DocumentFetchResult(document, true);
        DownloadFormListTask downloadFormListTask = initDownloadFormListTask(documentFetchResult);

        HashMap<String, FormDetails> formList = downloadFormListTask.doInBackground();

        String error = "root element namespace is incorrect:" + rootElement.getNamespace();
        String expectedErrorStr = Collect.getInstance().getString(R.string.parse_openrosa_formlist_failed, error);

        assertEquals(expectedErrorStr, formList.get(DL_ERROR_MSG).errorStr);
    }

}
