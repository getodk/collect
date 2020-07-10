package org.odk.collect.android.formmanagement;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.async.Scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void whenRepositoryStartSync_isSyncing_isTrue() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Scheduler.class), mock(FormRepository.class), mock(MediaFileRepository.class), mock(FormListApi.class), mock(FormDownloader.class), mock(DiskFormsSynchronizer.class), syncRepository, mock(PreferencesProvider.class));
        LiveData<Boolean> syncing = viewModel.isSyncing();

        syncRepository.startSync();
        assertThat(syncing.getValue(), is(true));
    }

    @Test
    public void syncWithServer_starsSyncOnRepository() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(fakeScheduler, mock(FormRepository.class), mock(MediaFileRepository.class), mock(FormListApi.class), mock(FormDownloader.class), mock(DiskFormsSynchronizer.class), syncRepository, mock(PreferencesProvider.class));

        LiveData<Boolean> syncing = syncRepository.isSyncing();
        viewModel.syncWithServer();
        assertThat(syncing.getValue(), is(true));
    }

    @Test
    public void syncWithServer_whenTaskFinishes_finishesSyncOnRepository() {
        SyncStatusRepository syncRepository = new SyncStatusRepository();
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(fakeScheduler, mock(FormRepository.class), mock(MediaFileRepository.class), mock(FormListApi.class), mock(FormDownloader.class), mock(DiskFormsSynchronizer.class), syncRepository, mock(PreferencesProvider.class));

        LiveData<Boolean> syncing = syncRepository.isSyncing();
        viewModel.syncWithServer();

        fakeScheduler.runBackgroundTask();
        assertThat(syncing.getValue(), is(false));
    }
}