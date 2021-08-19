package org.odk.collect.android.audio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.ExternalWebPageHelper;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder;
import org.odk.collect.fragmentstest.DialogFragmentTest;
import org.odk.collect.utilities.Clock;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingControllerFragmentTest {

    public StubAudioRecorder audioRecorder;
    private BackgroundAudioViewModel backgroundAudioViewModel;
    private FormEntryViewModel formEntryViewModel;
    private MutableNonNullLiveData<Boolean> hasBackgroundRecording;
    private MutableNonNullLiveData<Boolean> isBackgroundRecordingEnabled;
    private ExternalWebPageHelper externalWebPageHelper;

    @Before
    public void setup() throws IOException {
        File stubRecording = File.createTempFile("test", ".m4a");
        stubRecording.deleteOnExit();

        audioRecorder = new StubAudioRecorder(stubRecording.getAbsolutePath());
        backgroundAudioViewModel = mock(BackgroundAudioViewModel.class);
        formEntryViewModel = mock(FormEntryViewModel.class);

        hasBackgroundRecording = new MutableNonNullLiveData<>(false);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(hasBackgroundRecording);
        isBackgroundRecordingEnabled = new MutableNonNullLiveData<>(false);
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(isBackgroundRecordingEnabled);

        externalWebPageHelper = mock(ExternalWebPageHelper.class);

        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
                return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getGeneralSettings(), permissionsChecker, clock) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) backgroundAudioViewModel;
                    }
                };
            }

            @Override
            public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics) {
                return new FormEntryViewModel.Factory(clock) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formEntryViewModel;
                    }
                };
            }

            @Override
            public AudioRecorder providesAudioRecorder(Application application) {
                return audioRecorder;
            }

            @Override
            public ExternalWebPageHelper providesExternalWebPageHelper() {
                return externalWebPageHelper;
            }
        });
    }

    @Test
    public void updatesTimecode() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.timeCode.getText().toString(), equalTo("00:00"));

            audioRecorder.setDuration(40000);
            assertThat(fragment.binding.timeCode.getText().toString(), equalTo("00:40"));
        });
    }

    @Test
    public void updatesWaveform() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.volumeBar.getLatestAmplitude(), equalTo(0));

            audioRecorder.setAmplitude(156);
            assertThat(fragment.binding.volumeBar.getLatestAmplitude(), equalTo(156));
        });
    }

    @Test
    public void clickingPause_pausesRecording() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorder.getCurrentSession().getValue().getPaused(), is(true));
        });
    }

    @Test
    public void whenRecordingPaused_clickingPause_resumesRecording() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorder.getCurrentSession().getValue().getPaused(), is(false));
        });
    }

    @Test
    public void whenRecordingPaused_pauseIconChangesToResume() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.pauseRecording.getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
            assertThat(fragment.binding.pauseRecording.getContentDescription(), is(fragment.getString(R.string.resume_recording)));
        });
    }

    @Test
    public void whenRecordingPaused_recordingStatusChangesToPaused() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_pause_24dp));
        });
    }

    @Test
    public void whenRecordingResumed_pauseIconChangesToPause() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();
        audioRecorder.resume();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.pauseRecording.getIcon()).getCreatedFromResId(), is(R.drawable.ic_pause_24dp));
            assertThat(fragment.binding.pauseRecording.getContentDescription(), is(fragment.getString(R.string.pause_recording)));
        });
    }

    @Test
    public void whenRecordingResumed_recordingStatusChangesToRecording() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();
        audioRecorder.resume();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
        });
    }

    @Test
    @Config(sdk = 23)
    public void whenSDKOlderThan24_hidesPauseButton() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.GONE));
        });
    }

    @Test
    @Config(sdk = 24)
    public void whenSDK24OrNewer_showsPauseButton() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.VISIBLE));
        });
    }

    @Test
    public void whenFormHasBackgroundRecording_hidesControls() {
        hasBackgroundRecording.setValue(true);
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.controls.getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenFormHasBackgroundRecording_clickingHelpButton_opensHelpDialog() {
        hasBackgroundRecording.setValue(true);
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.help.getVisibility(), is(View.VISIBLE));

            fragment.binding.help.performClick();
            BackgroundAudioHelpDialogFragment dialog = getFragmentByClass(fragment.getParentFragmentManager(), BackgroundAudioHelpDialogFragment.class);
            assertThat(dialog, notNullValue());
        });
    }

    @Test
    public void whenFormDoesNotHaveBackgroundRecording_hidesHelpButton() {
        hasBackgroundRecording.setValue(false);
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.help.getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenThereIsAnErrorStartingRecording_showsErrorDialog() {
        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);

        audioRecorder.failOnStart();
        audioRecorder.start("blah", Output.AAC);
        scenario.onFragment(fragment -> {
            AudioRecordingErrorDialogFragment dialog = getFragmentByClass(fragment.getParentFragmentManager(), AudioRecordingErrorDialogFragment.class);
            assertThat(dialog, notNullValue());
        });
    }

    @Test
    public void whenFormHasBackgroundRecording_andBackgroundRecordingIsDisabled_showsThatRecordingIsDisabled() {
        hasBackgroundRecording.setValue(true);
        isBackgroundRecordingEnabled.setValue(false);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(fragment.binding.timeCode.getText(), is(fragment.getString(R.string.recording_disabled, "⋮")));
            assertThat(fragment.binding.volumeBar.getVisibility(), is(View.GONE));
            assertThat(fragment.binding.controls.getVisibility(), is(View.GONE));
            assertThat(fragment.binding.help.getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenFormDoesNotHaveBackgroundRecording_andBackgroundRecordingIsDisabled_doesNotShowRecordingIsDisabled() {
        hasBackgroundRecording.setValue(false);
        isBackgroundRecordingEnabled.setValue(false);

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenFormHasBackgroundRecording_andThereIsAnError_andSessionIsOver_showsThatThereIsAnError() {
        hasBackgroundRecording.setValue(true);
        isBackgroundRecordingEnabled.setValue(true);
        audioRecorder.failOnStart();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);

        audioRecorder.start("blah", Output.AAC_LOW);
        audioRecorder.cleanUp();

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(fragment.binding.timeCode.getText(), is(fragment.getString(R.string.start_recording_failed)));
            assertThat(fragment.binding.volumeBar.getVisibility(), is(View.GONE));
            assertThat(fragment.binding.controls.getVisibility(), is(View.GONE));
            assertThat(fragment.binding.help.getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenFormDoesNotHaveBackgroundRecording_andThereIsAnError_andSessionIsOver_doesNotThatThereIsAnError() {
        hasBackgroundRecording.setValue(false);
        isBackgroundRecordingEnabled.setValue(true);
        audioRecorder.failOnStart();

        FragmentScenario<AudioRecordingControllerFragment> scenario = DialogFragmentTest.launchDialogFragment(AudioRecordingControllerFragment.class);

        audioRecorder.start("blah", Output.AAC_LOW);
        audioRecorder.cleanUp();

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.GONE));
        });
    }
}
