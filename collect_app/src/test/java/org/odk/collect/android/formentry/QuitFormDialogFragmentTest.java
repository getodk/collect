package org.odk.collect.android.formentry;

import android.content.DialogInterface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.fragmentstest.DialogFragmentTest;
import org.odk.collect.testshared.RobolectricHelpers;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class QuitFormDialogFragmentTest {

    private final FormSaveViewModel formSaveViewModel = mock(FormSaveViewModel.class);

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder, CurrentProjectProvider currentProjectProvider) {
                return (owner, defaultArgs) -> new ViewModelProvider.Factory() {

                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formSaveViewModel;
                    }
                };
            }
        });
    }

    @Test
    public void shouldShowCorrectButtons() {
        FragmentScenario<QuitFormDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(QuitFormDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
            assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(GONE));
            assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(VISIBLE));
            assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(), equalTo(fragment.getString(R.string.do_not_exit)));
        });
    }

    @Test
    public void shouldShowCorrectTitle_whenNoFormIsLoaded() {
        FragmentScenario<QuitFormDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(QuitFormDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            TextView dialogTitle = dialog.findViewById(R.id.alertTitle);
            assertThat(dialogTitle.getText().toString(), equalTo(fragment.getString(R.string.quit_application, fragment.getString(R.string.no_form_loaded))));
        });
    }

    @Test
    public void shouldShowCorrectTitle_whenFormIsLoaded() {
        when(formSaveViewModel.getFormName()).thenReturn("blah");

        FragmentScenario<QuitFormDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(QuitFormDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            TextView dialogTitle = dialog.findViewById(R.id.alertTitle);
            assertThat(dialogTitle.getText().toString(), equalTo(fragment.getString(R.string.quit_application, "blah")));
        });
    }

    @Test
    public void dialogIsCancellable() {
        FragmentScenario<QuitFormDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(QuitFormDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(true));
        });
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        FragmentScenario<QuitFormDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(QuitFormDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            assertTrue(dialog.isShowing());

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            RobolectricHelpers.runLooper();
            assertFalse(dialog.isShowing());
        });
    }
}
