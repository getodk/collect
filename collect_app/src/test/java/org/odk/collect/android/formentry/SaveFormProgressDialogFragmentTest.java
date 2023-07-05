package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;

@RunWith(AndroidJUnit4.class)
public class SaveFormProgressDialogFragmentTest {

    private final FormSaveViewModel formSaveViewModel = mock(FormSaveViewModel.class);
    private final ViewModelProvider.Factory viewModelFactory = new ViewModelProvider.Factory() {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
            return (T) formSaveViewModel;
        }
    };

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule(
            com.google.android.material.R.style.Theme_MaterialComponents,
            new FragmentFactoryBuilder()
                    .forClass(SaveFormProgressDialogFragment.class, () -> new SaveFormProgressDialogFragment(viewModelFactory))
                    .build()
    );

    @Before
    public void setup() {
        when(formSaveViewModel.getSaveResult()).thenReturn(new MutableLiveData<>());
    }

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<SaveFormProgressDialogFragment> fragmentScenario = launcherRule.launch(SaveFormProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
