package org.odk.collect.android.formentry;

import android.content.DialogInterface;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.utilities.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioPermissionDialogFragmentTest {

    private FormEntryViewModel formEntryViewModel;
    private FakePermissionsProvider fakePermissionsProvider;

    @Before
    public void setup() {
        formEntryViewModel = mock(FormEntryViewModel.class);
        fakePermissionsProvider = new FakePermissionsProvider();

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics, PreferencesProvider preferencesProvider, AudioRecorder audioRecorder, PermissionsChecker permissionsChecker) {
                return new FormEntryViewModel.Factory(clock, analytics, preferencesProvider, audioRecorder, permissionsChecker) {

                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formEntryViewModel;
                    }
                };
            }

            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
                return fakePermissionsProvider;
            }
        });
    }

    @Test
    public void isNotCancellable() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.isCancelable(), is(false));
        });
    }

    @Test
    public void clickingOk_andGrantingPermissions_startsBackgroundRecording() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            fakePermissionsProvider.setPermissionGranted(true);
            button.performClick();

            verify(formEntryViewModel).startBackgroundRecording();
        });
    }

    @Test
    public void clickingOk_clearsError() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            fakePermissionsProvider.setPermissionGranted(true);
            button.performClick();

            verify(formEntryViewModel).errorDisplayed();
        });
    }
}
