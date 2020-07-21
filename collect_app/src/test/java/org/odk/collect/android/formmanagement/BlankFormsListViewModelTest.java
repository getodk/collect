package org.odk.collect.android.formmanagement;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
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

    private final SyncStatusRepository syncRepository = mock(SyncStatusRepository.class);

    @Before
    public void setup() {
        when(syncRepository.startSync()).thenReturn(true);
    }

    @Test
    public void isSyncing_whenRepositoryIsSyncing_isTrue() {
        when(syncRepository.isSyncing()).thenReturn(new MutableLiveData<>(true));

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));
        assertThat(viewModel.isSyncing().getValue(), is(true));
    }

    @Test
    public void syncWithServer_startsSyncOnRepository() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));

        viewModel.syncWithServer();
        verify(syncRepository).startSync();
    }

    @Test
    public void syncWithServer_whenTaskFinishes_finishesSyncOnRepository() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class));
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verify(syncRepository).finishSync();
    }

    @Test
    public void syncWithServer_whenStartSyncReturnsFalse_doesNothing() throws Exception {
        ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, serverFormsSynchronizer, mock(PreferencesProvider.class));

        when(syncRepository.startSync()).thenReturn(false);
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verify(serverFormsSynchronizer, never()).synchronize();
        verify(syncRepository, never()).finishSync();
    }
}