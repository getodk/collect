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
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.async.Scheduler;
import org.odk.collect.testshared.FakeScheduler;
import org.odk.collect.testshared.LiveDataTester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final LiveDataTester liveDataTester = new LiveDataTester();

    private final SyncStatusAppState syncRepository = mock(SyncStatusAppState.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();
    private final Analytics analytics = mock(Analytics.class);
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void isSyncing_followsRepositoryIsSyncing() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(true);
        when(syncRepository.isSyncing()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, settingsProvider, analytics, mock(FormUpdateChecker.class));
        assertThat(viewModel.isSyncing().getValue(), is(true));

        liveData.setValue(false);
        assertThat(viewModel.isSyncing().getValue(), is(false));
    }

    @Test
    public void isOutOfSync_followsRepositorySyncError() {
        MutableLiveData<FormSourceException> liveData = new MutableLiveData<>(new FormSourceException.FetchError());
        when(syncRepository.getSyncError()).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, settingsProvider, analytics, mock(FormUpdateChecker.class));
        LiveData<Boolean> outOfSync = liveDataTester.activate(viewModel.isOutOfSync());

        assertThat(outOfSync.getValue(), is(true));

        liveData.setValue(null);
        assertThat(outOfSync.getValue(), is(false));
    }

    @Test
    public void syncWithServer_whenTaskFinishes_setsResultToTrue() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        FormUpdateChecker formUpdateChecker = mock(FormUpdateChecker.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, settingsProvider, analytics, formUpdateChecker);

        doReturn(true).when(formUpdateChecker).synchronize();
        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(true));
    }

    @Test
    public void syncWithServer_whenThereIsAnError_setsResultToFalse() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        FormUpdateChecker formUpdateChecker = mock(FormUpdateChecker.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, settingsProvider, analytics, formUpdateChecker);

        doReturn(false).when(formUpdateChecker).synchronize();
        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(false));
    }
}
