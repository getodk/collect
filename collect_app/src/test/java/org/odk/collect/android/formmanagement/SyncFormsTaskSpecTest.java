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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SyncFormsTaskSpecTest {

    private ServerFormsSynchronizer serverFormsSynchronizer;
    private SyncStatusRepository syncStatusRepository;

    @Before
    public void setup() {
        serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        syncStatusRepository = mock(SyncStatusRepository.class);
        when(syncStatusRepository.startSync()).thenReturn(true);

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormRepository formRepository, FormDownloader formDownloader) {
                return serverFormsSynchronizer;
            }

            @Override
            public SyncStatusRepository providesServerFormSyncRepository() {
                return syncStatusRepository;
            }
        });
    }

    @Test
    public void setsRepositoryToSyncing_runsSync_thenSetsRepositoryToNotSyncing() throws Exception {
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Runnable task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.run();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync();
    }

    @Test
    public void whenSynchronizingFails_setsRepositoryToNotSyncing() throws Exception {
        doThrow(new FormApiException(FormApiException.Type.AUTH_REQUIRED, "")).when(serverFormsSynchronizer).synchronize();
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Runnable task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.run();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync();
    }

    @Test
    public void whenStartSyncReturnsFalse_doesNothing() throws Exception {
        when(syncStatusRepository.startSync()).thenReturn(false);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Runnable task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.run();

        verify(serverFormsSynchronizer, never()).synchronize();
        verify(syncStatusRepository, never()).finishSync();
    }
}