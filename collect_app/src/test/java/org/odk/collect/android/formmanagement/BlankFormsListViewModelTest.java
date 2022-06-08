package org.odk.collect.android.formmanagement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.androidtest.LiveDataTestUtilsKt.getOrAwaitValue;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.projects.Project;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.testshared.FakeScheduler;

@RunWith(AndroidJUnit4.class)
public class BlankFormsListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final SyncStatusAppState syncRepository = mock(SyncStatusAppState.class);
    private final Analytics analytics = mock(Analytics.class);
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();
    private final CurrentProjectProvider currentProjectProvider = mock(CurrentProjectProvider.class);

    @Before
    public void setup() {
        when(currentProjectProvider.getCurrentProject()).thenReturn(new Project.Saved("testProject", "Test Project", "T", "#ffffff"));
    }

    @Test
    public void isSyncing_followsRepositoryIsSyncing() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(true);
        when(syncRepository.isSyncing("testProject")).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, settingsProvider, analytics, mock(FormsUpdater.class), currentProjectProvider, null);
        assertThat(viewModel.isSyncing().getValue(), is(true));

        liveData.setValue(false);
        assertThat(viewModel.isSyncing().getValue(), is(false));
    }

    @Test
    public void isOutOfSync_followsRepositorySyncError() {
        MutableLiveData<FormSourceException> liveData = new MutableLiveData<>(new FormSourceException.FetchError());
        when(syncRepository.getSyncError("testProject")).thenReturn(liveData);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), mock(Scheduler.class), syncRepository, settingsProvider, analytics, mock(FormsUpdater.class), currentProjectProvider, null);
        LiveData<Boolean> outOfSync = viewModel.isOutOfSync();

        assertThat(getOrAwaitValue(outOfSync), is(true));

        liveData.setValue(null);
        assertThat(getOrAwaitValue(outOfSync), is(false));
    }

    @Test
    public void syncWithServer_whenTaskFinishes_setsResultToTrue() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        FormsUpdater formsUpdater = mock(FormsUpdater.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, settingsProvider, analytics, formsUpdater, currentProjectProvider, null);

        doReturn(true).when(formsUpdater).matchFormsWithServer("testProject");
        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(true));
    }

    @Test
    public void syncWithServer_whenThereIsAnError_setsResultToFalse() {
        FakeScheduler fakeScheduler = new FakeScheduler();
        FormsUpdater formsUpdater = mock(FormsUpdater.class);

        BlankFormsListViewModel viewModel = new BlankFormsListViewModel(mock(Application.class), fakeScheduler, syncRepository, settingsProvider, analytics, formsUpdater, currentProjectProvider, null);

        doReturn(false).when(formsUpdater).matchFormsWithServer("testProject");
        LiveData<Boolean> result = viewModel.syncWithServer();
        fakeScheduler.runBackground();

        assertThat(result.getValue(), is(false));
    }
}
