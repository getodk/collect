package org.odk.collect.android.formmanagement;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.async.Scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void isSyncing_whenRepositoryStartSync_isTrue() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));
        LiveData<Boolean> syncing = viewModel.isSyncing();

        syncRepository.startSync();
        assertThat(syncing.getValue(), is(true));
    }

    @Test
    public void syncWithServer_startsSyncOnRepository() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));

        LiveData<Boolean> syncing = syncRepository.isSyncing();
        viewModel.syncWithServer();
        assertThat(syncing.getValue(), is(true));
    }

    @Test
    public void syncWithServer_whenTaskFinishes_finishesSyncOnRepository() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));

        LiveData<Boolean> syncing = syncRepository.isSyncing();
        viewModel.syncWithServer();

        fakeScheduler.runBackgroundTask();
        assertThat(syncing.getValue(), is(false));
    }

    @Test
    public void syncWithServer_whenStartSyncReturnsFalse_doesNothing() throws Exception {
        SyncStatusRepository syncRepository = mock(SyncStatusRepository.class);
        ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(fakeScheduler, syncRepository, serverFormsSynchronizer, mock(PreferencesProvider.class));

        when(syncRepository.startSync()).thenReturn(false);
        viewModel.syncWithServer();

        fakeScheduler.runBackgroundTask();
        verify(serverFormsSynchronizer, never()).synchronize();
        verify(syncRepository, never()).finishSync();
    }
}