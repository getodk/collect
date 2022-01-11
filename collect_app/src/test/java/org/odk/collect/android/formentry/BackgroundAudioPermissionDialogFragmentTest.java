package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.DialogInterface;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.testshared.RobolectricHelpers;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioPermissionDialogFragmentTest {

    private BackgroundAudioViewModel backgroundAudioViewModel;
    private FakePermissionsProvider fakePermissionsProvider;

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Before
    public void setup() {
        backgroundAudioViewModel = mock(BackgroundAudioViewModel.class);
        fakePermissionsProvider = new FakePermissionsProvider();

        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Analytics analytics) {
                return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getUnprotectedSettings(), permissionsChecker, System::currentTimeMillis) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) backgroundAudioViewModel;
                    }
                };
            }

            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
                return fakePermissionsProvider;
            }
        });
    }

    @Test
    public void isNotCancellable() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = launcherRule.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.isCancelable(), is(false));
        });
    }

    @Test
    public void clickingOk_andGrantingPermissions_callsGrantPermission() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = launcherRule.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            fakePermissionsProvider.setPermissionGranted(true);

            button.performClick();
            RobolectricHelpers.runLooper();
            verify(backgroundAudioViewModel).grantAudioPermission();
        });
    }

    @Test
    public void clickingOk_andGrantingPermissions_whenGrantPermissionsThrowsIllegalStateException_finishesActivity() {
        doThrow(IllegalStateException.class).when(backgroundAudioViewModel).grantAudioPermission();

        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = launcherRule.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            FragmentActivity activity = f.getActivity(); // Need to grab this here as `getActivity()` will return null later

            AlertDialog dialog = (AlertDialog) f.getDialog();
            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            fakePermissionsProvider.setPermissionGranted(true);

            button.performClick();
            RobolectricHelpers.runLooper();
            assertThat(activity.isFinishing(), is(true));
        });
    }
}
