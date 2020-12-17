package org.odk.collect.android.audio;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorderViewModel;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.android.support.RobolectricHelpers.getFragmentByClass;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingControllerFragmentTest {

    public StubAudioRecorderViewModel audioRecorderViewModel;

    @Before
    public void setup() throws IOException {
        File stubRecording = File.createTempFile("test", ".m4a");
        stubRecording.deleteOnExit();

        audioRecorderViewModel = new StubAudioRecorderViewModel(stubRecording.getAbsolutePath());

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public AudioRecorderViewModelFactory providesAudioRecorderViewModelFactory(Application application) {
                return new AudioRecorderViewModelFactory(application) {
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) audioRecorderViewModel;
                    }
                };
            }
        });

        // Needed to inflate views with theme attributes - needs to be a "real theme" because of DialogFragment
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);

        // View only shows when recording in progress
        audioRecorderViewModel.start("session", Output.AAC);
    }

    @Test
    public void updatesTimecode() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.timeCode.getText().toString(), equalTo("00:00"));

            audioRecorderViewModel.setDuration(40000);
            assertThat(fragment.binding.timeCode.getText().toString(), equalTo("00:40"));
        });
    }

    @Test
    public void clickingPause_pausesRecording() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorderViewModel.getCurrentSession().getValue().getPaused(), is(true));
        });
    }

    @Test
    public void whenRecordingPaused_clickingPause_resumesRecording() {
        audioRecorderViewModel.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorderViewModel.getCurrentSession().getValue().getPaused(), is(false));
        });
    }

    @Test
    public void whenRecordingPaused_pauseIconChangesToResume() {
        audioRecorderViewModel.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.pauseRecording.getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
            assertThat(fragment.binding.pauseRecording.getContentDescription(), is(fragment.getString(R.string.resume_recording)));
        });
    }

    @Test
    public void whenRecordingPaused_recordingStatusChangesToPaused() {
        audioRecorderViewModel.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingStatus.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_pause_24dp));
        });
    }

    @Test
    public void whenRecordingResumed_pauseIconChangesToPause() {
        audioRecorderViewModel.pause();
        audioRecorderViewModel.resume();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.pauseRecording.getIcon()).getCreatedFromResId(), is(R.drawable.ic_pause_24dp));
            assertThat(fragment.binding.pauseRecording.getContentDescription(), is(fragment.getString(R.string.pause_recording)));
        });
    }

    @Test
    public void whenRecordingResumed_recordingStatusChangesToRecording() {
        audioRecorderViewModel.pause();
        audioRecorderViewModel.resume();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingStatus.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
        });
    }

    @Test
    @Config(sdk = 23)
    public void whenSDKOlderThan24_hidesPauseButton() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.GONE));
        });
    }

    @Test
    @Config(sdk = 24)
    public void whenSDK24OrNewer_showsPauseButton() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.VISIBLE));
        });
    }

    @Test
    public void whenThereIsAnErrorStartingRecording_showsErrorDialog() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);

        audioRecorderViewModel.failOnStart();
        audioRecorderViewModel.start("blah", Output.AAC);
        scenario.onFragment(fragment -> {
            AudioRecordingErrorDialogFragment dialog = getFragmentByClass(fragment.getParentFragmentManager(), AudioRecordingErrorDialogFragment.class);
            assertThat(dialog, notNullValue());
        });
    }
}
