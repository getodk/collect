package org.odk.collect.android.formmanagement;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.android.backgroundwork.SyncFormsTaskSpec;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Supplier;

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
    private Notifier notifier;

    @Before
    public void setup() {
        notifier = mock(Notifier.class);
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

            @Override
            public Notifier providesNotifier(Application application) {
                return notifier;
            }
        });
    }

    @Test
    public void setsRepositoryToSyncing_runsSync_thenSetsRepositoryToNotSyncing() throws Exception {
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync(true);
    }

    @Test
    public void whenSynchronizingFails_setsRepositoryToNotSyncingAndNotifiesWithError() throws Exception {
        FormApiException exception = new FormApiException(FormApiException.Type.FETCH_ERROR);
        doThrow(exception).when(serverFormsSynchronizer).synchronize();
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync(false);
        verify(notifier).onSyncFailure(exception);
    }

    @Test
    public void whenStartSyncReturnsFalse_doesNothing() throws Exception {
        when(syncStatusRepository.startSync()).thenReturn(false);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(serverFormsSynchronizer, never()).synchronize();
        verify(syncStatusRepository, never()).finishSync(true);
    }
}