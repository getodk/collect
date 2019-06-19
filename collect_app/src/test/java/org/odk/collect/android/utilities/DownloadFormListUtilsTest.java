package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DownloadFormListUtilsTest {

    private CollectServerClient client = mock(CollectServerClient.class);

    @Before
    public void setup() {
        when(client.getXmlDocument(any())).thenReturn(new DocumentFetchResult("blah", 200));

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public CollectServerClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
                return client;
            }
        });
    }


    @Test
    public void removesTrailingSlashesFromUrl() {
        DownloadFormListUtils downloadFormListUtils = new DownloadFormListUtils();
        downloadFormListUtils.downloadFormList("http://blah.com///", "user", "password", false);

        verify(client).getXmlDocument("http://blah.com/formList");
    }
}