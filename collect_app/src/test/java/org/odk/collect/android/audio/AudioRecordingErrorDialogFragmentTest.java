package org.odk.collect.android.audio;

import android.app.Application;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorderViewModel;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingErrorDialogFragmentTest {

    private StubAudioRecorderViewModel viewModel;

    @Before
    public void setup() throws Exception {
        File stubRecording = File.createTempFile("test", ".m4a");
        stubRecording.deleteOnExit();
        viewModel = new StubAudioRecorderViewModel(stubRecording.getAbsolutePath());
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public AudioRecorderViewModelFactory providesAudioRecorderViewModelFactory(Application application) {
                return new AudioRecorderViewModelFactory(application) {
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) viewModel;
                    }
                };
            }
        });
    }

    @Test
    public void clickingOK_dismissesDialog() {
        FragmentScenario<AudioRecordingErrorDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(AudioRecordingErrorDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            button.performClick();
            assertThat(dialog.isShowing(), is(false));
        });
    }

    @Test
    public void onDismiss_callsCleanUpOnViewModel() {
        FragmentScenario<AudioRecordingErrorDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(AudioRecordingErrorDialogFragment.class);
        scenario.onFragment(DialogFragment::dismiss);
        assertThat(viewModel.getWasCleanedUp(), is(true));
    }
}
