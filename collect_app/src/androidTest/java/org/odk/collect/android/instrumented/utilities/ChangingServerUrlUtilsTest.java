package org.odk.collect.android.instrumented.utilities;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.utilities.ChangingServerUrlUtils;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.odk.collect.android.preferences.utilities.ChangingServerUrlUtils.KNOWN_URL_LIST;

public class ChangingServerUrlUtilsTest {

    @Before
    public void setUp() {
        GeneralSharedPreferences.getInstance().reset(KNOWN_URL_LIST);
    }

    @Test
    public void urlListShouldContainOnlyDefaultUrlIfNothingHasBeenAdded() {
        List<String> urlList = ChangingServerUrlUtils.getUrlList();
        assertEquals(1, urlList.size());
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(0));
    }

    @Test
    public void theLastUrlShouldBeRemovedWhenTheListHasMoreThan5Elements() {
        ChangingServerUrlUtils.addUrlToList("Url1");
        ChangingServerUrlUtils.addUrlToList("Url2");
        ChangingServerUrlUtils.addUrlToList("Url3");
        ChangingServerUrlUtils.addUrlToList("Url4");
        ChangingServerUrlUtils.addUrlToList("Url5");
        ChangingServerUrlUtils.addUrlToList("Url6");
        ChangingServerUrlUtils.addUrlToList("Url7");

        List<String> urlList = ChangingServerUrlUtils.getUrlList();
        assertEquals(5, urlList.size());

        assertEquals("Url3", urlList.get(4));
    }

    @Test
    public void urlListShouldContainUniqueValues() {
        ChangingServerUrlUtils.addUrlToList("Url1");
        ChangingServerUrlUtils.addUrlToList("Url1");
        assertEquals(2, ChangingServerUrlUtils.getUrlList().size());
    }

    @Test
    public void urlThatAlreadyExistsShouldBeMovedToTheTopOfTheList() {
        ChangingServerUrlUtils.addUrlToList("Url1");
        ChangingServerUrlUtils.addUrlToList("Url2");
        List<String> urlList = ChangingServerUrlUtils.getUrlList();
        assertEquals(3, urlList.size());
        assertEquals("Url2", urlList.get(0));
        assertEquals("Url1", urlList.get(1));
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(2));

        ChangingServerUrlUtils.addUrlToList("Url1");
        urlList = ChangingServerUrlUtils.getUrlList();
        assertEquals(3, urlList.size());
        assertEquals("Url1", urlList.get(0));
        assertEquals("Url2", urlList.get(1));
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(2));
    }
}
