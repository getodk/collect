package org.odk.collect.android.formmanagement;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesDataSourceProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.async.Scheduler;
import org.odk.collect.testshared.FakeScheduler;
import org.odk.collect.testshared.LiveDataTester;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final LiveDataTester liveDataTester = new LiveDataTester();

    private final SyncStatusRepository syncRepository = mock(SyncStatusRepository.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();
    private final Analytics analytics = mock(Analytics.class);
    private final PreferencesDataSourceProvider preferencesDataSourceProvider = TestPreferencesProvider.getPreferencesRepository();

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void isSyncing_followsRepositoryIsSyncing() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(true);
        when(syncRepository.isSyncing()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);
        assertThat(viewModel.isSyncing().getValue(), is(true));

        liveData.setValue(false);
        assertThat(viewModel.isSyncing().getValue(), is(false));
    }

    @Test
    public void isOutOfSync_followsRepositorySyncError() {
        MutableLiveData<FormSourceException> liveData = new MutableLiveData<>(new FormSourceException.FetchError());
        when(syncRepository.getSyncError()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);
        LiveData<Boolean> outOfSync = liveDataTester.activate(viewModel.isOutOfSync());

        assertThat(outOfSync.getValue(), is(true));

        liveData.setValue(null);
        assertThat(outOfSync.getValue(), is(false));
    }

    @Test
    public void syncWithServer_startsSyncOnRepository() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);

        viewModel.syncWithServer();
        verify(syncRepository).startSync();
    }

    @Test
    public void syncWithServer_whenTaskFinishes_finishesSyncOnRepositoryAndNotifies() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, notifier, changeLock, analytics);
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verify(syncRepository).finishSync(null);
        verify(notifier).onSync(null);
    }

    @Test
    public void syncWithServer_whenTaskFinishes_setsResultToTrue() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);
        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(true));
    }

    @Test
    public void syncWithServer_whenTaskFinishes_logsAnalytics() {
        FakeScheduler fakeScheduler = new FakeScheduler();

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "Success");
    }

    @Test
    public void syncWithServer_whenThereIsAnError_finishesSyncOnRepositoryWithFailureAndSendsErrorToNotifier() throws Exception {
        FakeScheduler fakeScheduler = new FakeScheduler();
        ServerFormsSynchronizer synchronizer = mock(ServerFormsSynchronizer.class);
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, synchronizer, preferencesDataSourceProvider, notifier, changeLock, analytics);

        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(synchronizer).synchronize();
        viewModel.syncWithServer();
        fakeScheduler.runBackground();

        verify(syncRepository).finishSync(exception);
        verify(notifier).onSync(exception);
    }

    @Test
    public void syncWithServer_whenThereIsAnError_logsAnalytics() throws Exception {
        FakeScheduler fakeScheduler = new FakeScheduler();
        ServerFormsSynchronizer synchronizer = mock(ServerFormsSynchronizer.class);
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, synchronizer, preferencesDataSourceProvider, notifier, changeLock, analytics);

        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(synchronizer).synchronize();
        viewModel.syncWithServer();
        fakeScheduler.runBackground();

        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "FETCH_ERROR");
    }

    @Test
    public void syncWithServer_whenThereIsAnError_setsResultToFalse() throws Exception {
        FakeScheduler fakeScheduler = new FakeScheduler();
        ServerFormsSynchronizer synchronizer = mock(ServerFormsSynchronizer.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, synchronizer, preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);

        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(synchronizer).synchronize();

        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(false));
    }

    @Test
    public void syncWithServer_whenChangeLockLocked_doesNothing() throws Exception {
        ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
        FakeScheduler fakeScheduler = new FakeScheduler();
        Notifier notifier = mock(Notifier.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, serverFormsSynchronizer, preferencesDataSourceProvider, notifier, changeLock, analytics);

        changeLock.lock();
        viewModel.syncWithServer();

        fakeScheduler.runBackground();
        verifyNoInteractions(serverFormsSynchronizer);
        verifyNoInteractions(syncRepository);
        verifyNoInteractions(notifier);
    }

    @Test
    public void syncWithServer_logsAnalytics() {
        preferencesDataSourceProvider.getGeneralPreferences().save(GeneralKeys.KEY_SERVER_URL, "https://blah.com/formServer");

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, mock(ServerFormsSynchronizer.class), preferencesDataSourceProvider, mock(Notifier.class), changeLock, analytics);
        viewModel.syncWithServer();
        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC, "Manual", "0053d8f5d460348d99c4cfb06dfa1eae"); // MD5 of blah.com (host)
    }
}
