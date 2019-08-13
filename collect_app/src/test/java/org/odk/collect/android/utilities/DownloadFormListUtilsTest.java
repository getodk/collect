package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.http.CollectServerClient;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DownloadFormListUtilsTest {

    private final CollectServerClient client = mock(CollectServerClient.class);

    @Before
    public void setup() {
        when(client.getXmlDocument(any())).thenReturn(new DocumentFetchResult("blah", 200));
    }

    @Test
    public void removesTrailingSlashesFromUrl() {
        DownloadFormListUtils downloadFormListUtils = new DownloadFormListUtils(
                RuntimeEnvironment.application,
                client,
                new WebCredentialsUtils(),
                new FormsDao()
        );

        downloadFormListUtils.downloadFormList("http://blah.com///", "user", "password", false);
        verify(client).getXmlDocument("http://blah.com/formList");
    }
}