package org.odk.collect.android.formmanagement;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class SyncFormsTaskSpecTest {

    private ServerFormsSynchronizer serverFormsSynchronizer;
    private ServerFormsSyncRepository serverFormsSyncRepository;

    @Before
    public void setup() {
        serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        serverFormsSyncRepository = mock(ServerFormsSyncRepository.class);

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormRepository formRepository, FormDownloader formDownloader) {
                return serverFormsSynchronizer;
            }

            @Override
            public ServerFormsSyncRepository providesServerFormSyncRepository() {
                return serverFormsSyncRepository;
            }
        });
    }

    @Test
    public void setsRepositoryToSyncing_runsSync_thenSetsRepositoryToNotSyncing() throws Exception {
        InOrder inOrder = inOrder(serverFormsSyncRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Runnable task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.run();

        inOrder.verify(serverFormsSyncRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(serverFormsSyncRepository).finishSync();
    }

    @Test
    public void whenSynchronizingFails_setsRepositoryToNotSyncing() throws Exception {
        doThrow(new FormApiException(FormApiException.Type.AUTH_REQUIRED, "")).when(serverFormsSynchronizer).synchronize();
        InOrder inOrder = inOrder(serverFormsSyncRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Runnable task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.run();

        inOrder.verify(serverFormsSyncRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(serverFormsSyncRepository).finishSync();
    }
}