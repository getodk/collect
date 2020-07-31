package org.odk.collect.android.formmanagement;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.android.support.LiveDataTester;
import org.odk.collect.async.Scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final LiveDataTester liveDataTester = new LiveDataTester();

    private final SyncStatusRepository syncRepository = mock(SyncStatusRepository.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void isSyncing_followsRepositoryIsSyncing() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(true);
        when(syncRepository.isSyncing()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class), mock(Notifier.class), changeLock);
        assertThat(viewModel.isSyncing().getValue(), is(true));

        liveData.setValue(false);
        assertThat(viewModel.isSyncing().getValue(), is(false));
    }

    @Test
    public void isOutOfSync_followsRepositorySyncError() {
        MutableLiveData<FormApiException> liveData = new MutableLiveData<>(new FormApiException(FormApiException.Type.FETCH_ERROR));
        when(syncRepository.getSyncError()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class), mock(Notifier.class), changeLock);
        LiveData<Boolean> outOfSync = liveDataTester.activate(viewModel.isOutOfSync());

        assertThat(outOfSync.getValue(), is(true));

        liveData.setValue(null);
        assertThat(outOfSync.getValue(), is(false));
    }

    @Test
    public void syncWithServer_startsSyncOnRepository() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class), mock(Notifier.class), changeLock);

        viewModel.syncWithServer();
        verify(syncRepository).startSync();
    }

    @Test
    public void syncWithServer_whenTaskFinishes_finishesSyncOnRepository() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), mock(PreferencesProvider.class), notifier, changeLock);
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verify(syncRepository).finishSync(null);
        verify(notifier).onSync(null);
    }

    @Test
    public void syncWithServer_whenThereIsAnError_finishesSyncOnRepositoryWithFailureAndSendsErrorToNotifier() throws Exception {
        FakeScheduler fakeScheduler = new FakeScheduler();
        ServerFormsSynchronizer synchronizer = mock(ServerFormsSynchronizer.class);
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, synchronizer, mock(PreferencesProvider.class), notifier, changeLock);

        FormApiException exception = new FormApiException(FormApiException.Type.FETCH_ERROR);
        doThrow(exception).when(synchronizer).synchronize();
        viewModel.syncWithServer();
        fakeScheduler.runBackground();

        verify(syncRepository).finishSync(exception);
        verify(notifier).onSync(exception);
    }

    @Test
    public void syncWithServer_whenChangeLockLocked_doesNothing() throws Exception {
        ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        FakeScheduler fakeScheduler = new FakeScheduler();
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, serverFormsSynchronizer, mock(PreferencesProvider.class), notifier, changeLock);

        changeLock.lock();
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verifyNoInteractions(serverFormsSynchronizer);
        verifyNoInteractions(syncRepository);
        verifyNoInteractions(notifier);
    }
}