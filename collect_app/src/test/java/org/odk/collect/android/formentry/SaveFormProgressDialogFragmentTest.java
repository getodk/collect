package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.fragmentstest.DialogFragmentTest;

@RunWith(AndroidJUnit4.class)
public class SaveFormProgressDialogFragmentTest {

    private final FormSaveViewModel formSaveViewModel = mock(FormSaveViewModel.class);

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder, CurrentProjectProvider currentProjectProvider, MediaUtils mediaUtils, InstancesRepositoryProvider instancesRepositoryProvider) {
                return (owner, defaultArgs) -> new ViewModelProvider.Factory() {

                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formSaveViewModel;
                    }
                };
            }
        });
        when(formSaveViewModel.getSaveResult()).thenReturn(mock(LiveData.class));
    }

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<SaveFormProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(SaveFormProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
