package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.savedstate.SavedStateRegistryOwner;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.entities.EntitiesRepositoryProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;

@RunWith(AndroidJUnit4.class)
public class SaveFormProgressDialogFragmentTest {

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule(
            R.style.Theme_MaterialComponents
    );

    @Before
    public void setup() {
        FormSaveViewModel formSaveViewModel = mock(FormSaveViewModel.class);
        when(formSaveViewModel.getSaveResult()).thenReturn(new MutableLiveData<>());

        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder, CurrentProjectProvider currentProjectProvider, MediaUtils mediaUtils, FormSessionRepository formSessionRepository, EntitiesRepositoryProvider entitiesRepositoryProvider) {
                return new FormSaveViewModel.FactoryFactory() {
                    @Override
                    public void setSessionId(String sessionId) {

                    }

                    @Override
                    public ViewModelProvider.Factory create(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs) {
                        return new ViewModelProvider.Factory() {

                            @NonNull
                            @Override
                            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                                return (T) formSaveViewModel;
                            }
                        };
                    }
                };
            }
        });
    }

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<SaveFormProgressDialogFragment> fragmentScenario = launcherRule.launch(SaveFormProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
