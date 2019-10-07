package org.odk.collect.android.preferences.utilities;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.odk.collect.android.preferences.utilities.KnownUrlListUtils.KNOWN_URL_LIST;

@RunWith(AndroidJUnit4.class)
public class KnownUrlListUtilsTest {

    @Before
    public void setUp() {
        GeneralSharedPreferences.getInstance().reset(KNOWN_URL_LIST);
    }

    @Test
    public void urlListShouldContainOnlyDefaultUrlIfNothingHasBeenAdded() {
        List<String> urlList = KnownUrlListUtils.getUrlList();
        assertEquals(1, urlList.size());
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(0));
    }

    @Test
    public void theLastUrlShouldBeRemovedWhenTheListHasMoreThan5ElementsButNotTheDefaultUrl() {
        KnownUrlListUtils.addUrlToList("Url1");
        KnownUrlListUtils.addUrlToList("Url2");
        KnownUrlListUtils.addUrlToList("Url3");
        KnownUrlListUtils.addUrlToList("Url4");
        KnownUrlListUtils.addUrlToList("Url5");
        KnownUrlListUtils.addUrlToList("Url6");
        KnownUrlListUtils.addUrlToList("Url7");

        List<String> urlList = KnownUrlListUtils.getUrlList();
        assertEquals(5, urlList.size());

        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(4));
    }

    @Test
    public void urlListShouldContainUniqueValues() {
        KnownUrlListUtils.addUrlToList("Url1");
        KnownUrlListUtils.addUrlToList("Url1");
        assertEquals(2, KnownUrlListUtils.getUrlList().size());
    }

    @Test
    public void urlThatAlreadyExistsShouldBeMovedToTheTopOfTheList() {
        KnownUrlListUtils.addUrlToList("Url1");
        KnownUrlListUtils.addUrlToList("Url2");
        List<String> urlList = KnownUrlListUtils.getUrlList();
        assertEquals(3, urlList.size());
        assertEquals("Url2", urlList.get(0));
        assertEquals("Url1", urlList.get(1));
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(2));

        KnownUrlListUtils.addUrlToList("Url1");
        urlList = KnownUrlListUtils.getUrlList();
        assertEquals(3, urlList.size());
        assertEquals("Url1", urlList.get(0));
        assertEquals("Url2", urlList.get(1));
        assertEquals(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.default_server_url), urlList.get(2));
    }
}
