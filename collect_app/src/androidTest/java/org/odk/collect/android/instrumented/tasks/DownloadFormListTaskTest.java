package org.odk.collect.android.instrumented.tasks;

import org.junit.Test;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DownloadFormListTaskTest {

    @Test
    public void whenAlternateCredentialsAreSet_shouldServerFormsDetailsFetcherBeUpdated() {
        ServerFormsDetailsFetcher serverFormsDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        DownloadFormListTask task = new DownloadFormListTask(serverFormsDetailsFetcher);
        task.setAlternateCredentials(webCredentialsUtils, "https://test-server.com", "testUser", "testPassword");
        verify(serverFormsDetailsFetcher).updateFormListApi("https://test-server.com", webCredentialsUtils);
    }

}
