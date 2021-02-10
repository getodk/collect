package org.odk.collect.android.audio;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder;
import org.odk.collect.utilities.Clock;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.getFragmentByClass;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingControllerFragmentTest {

    public StubAudioRecorder audioRecorder;
    private BackgroundAudioViewModel backgroundAudioViewModel;
    private FormEntryViewModel formEntryViewModel;

    @Before
    public void setup() throws IOException {
        File stubRecording = File.createTempFile("test", ".m4a");
        stubRecording.deleteOnExit();

        audioRecorder = new StubAudioRecorder(stubRecording.getAbsolutePath());

        backgroundAudioViewModel = mock(BackgroundAudioViewModel.class);
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(true));

        formEntryViewModel = mock(FormEntryViewModel.class);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(new MutableLiveData<>(true));

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, PreferencesProvider preferencesProvider, PermissionsChecker permissionsChecker) {
                return new BackgroundAudioViewModel.Factory(audioRecorder, preferencesProvider, permissionsChecker) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) backgroundAudioViewModel;
                    }
                };
            }

            @Override
            public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics) {
                return new FormEntryViewModel.Factory(clock, analytics) {
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
        });

        // Needed to inflate views with theme attributes - needs to be a "real theme" because of DialogFragment
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);
    }

    @Test
    public void updatesTimecode() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.recordingStatusMessage.getText().toString(), equalTo("00:00"));

            audioRecorder.setDuration(40000);
            assertThat(fragment.binding.recordingStatusMessage.getText().toString(), equalTo("00:40"));
        });
    }

    @Test
    public void updatesWaveform() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.waveform.getLatestAmplitude(), equalTo(0));

            audioRecorder.setAmplitude(156);
            assertThat(fragment.binding.waveform.getLatestAmplitude(), equalTo(156));
        });
    }

    @Test
    public void clickingPause_pausesRecording() {
        audioRecorder.start("session", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorder.getCurrentSession().getValue().getPaused(), is(true));
        });
    }

    @Test
    public void whenRecordingPaused_clickingPause_resumesRecording() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            fragment.binding.pauseRecording.performClick();
            assertThat(audioRecorder.getCurrentSession().getValue().getPaused(), is(false));
        });
    }

    @Test
    public void whenRecordingPaused_pauseIconChangesToResume() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.pauseRecording.getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
            assertThat(fragment.binding.pauseRecording.getContentDescription(), is(fragment.getString(R.string.resume_recording)));
        });
    }

    @Test
    public void whenRecordingPaused_recordingStatusChangesToPaused() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_pause_24dp));
        });
    }

    @Test
    public void whenRecordingResumed_pauseIconChangesToPause() {
        audioRecorder.start("session", Output.AAC);
        audioRecorder.pause();
        audioRecorder.resume();

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
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

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
        });
    }

    @Test
    @Config(sdk = 23)
    public void whenSDKOlderThan24_hidesPauseButton() {
        audioRecorder.start("session", Output.AAC);
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.GONE));
        });
    }

    @Test
    @Config(sdk = 24)
    public void whenSDK24OrNewer_showsPauseButton() {
        audioRecorder.start("session", Output.AAC);
        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.pauseRecording.getVisibility(), is(View.VISIBLE));
        });
    }

    @Test
    public void whenThereIsAnErrorStartingRecording_showsErrorDialog() {
        audioRecorder.cleanUp(); // Reset recorder

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);

        audioRecorder.failOnStart();
        audioRecorder.start("blah", Output.AAC);
        scenario.onFragment(fragment -> {
            AudioRecordingErrorDialogFragment dialog = getFragmentByClass(fragment.getParentFragmentManager(), AudioRecordingErrorDialogFragment.class);
            assertThat(dialog, notNullValue());
        });
    }

    //region Options menu override for background recording
    @Test
    public void whenRecordAudioFalse_andBackgroundRecordingRequested_displaysError() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(false));

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true); // FormController is set
        formControllerSet.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(fragment.binding.recordingStatusMessage.getText(), is("Recording disabled. Enable in ⋮"));
        });
    }

    @Test
    public void whenRecordAudioFalse_andBackgroundRecordingRequested_displaysNoRecordingIcon() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(false));

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true); // FormController is set
        formControllerSet.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_off_24));
        });
    }

    @Test
    public void whenRecordAudioFalse_andBackgroundRecordingNotRequested_hidesAudioBar() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(false));

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        formControllerSet.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenRecordAudioToggledToFalse_andBackgroundRecordingRequested_displaysError() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true);

        MutableLiveData<Boolean> backgroundRecordingEnabled = new MutableLiveData<>(true);
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(backgroundRecordingEnabled);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        backgroundRecordingEnabled.postValue(false);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.recordingStatusMessage.getText(), is("Recording disabled. Enable in ⋮"));
        });
    }

    @Test
    public void whenRecordAudioTrue_andBackgroundRecordingRequested_displaysRecordingIcon() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(true));
        audioRecorder.start("background", Output.AAC);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true); // FormController is set
        formControllerSet.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(shadowOf(fragment.binding.recordingIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.ic_baseline_mic_24));
        });
    }

    @Test
    public void whenRecordAudioTrue_andBackgroundRecordingNotRequested_hidesAudioBar() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableLiveData<>(true));

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        formControllerSet.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.GONE));
        });
    }

    @Test
    public void whenRecordAudioToggledToTrue_andBackgroundRecordingRequested_displaysError() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true);

        MutableLiveData<Boolean> backgroundRecordingEnabled = new MutableLiveData<>(false);
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(backgroundRecordingEnabled);

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        backgroundRecordingEnabled.postValue(true);

        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.recordingStatusMessage.getText(), is("Recording disabled. Enable in ⋮"));
        });
    }
    //endregion

    @Test
    public void whenThereIsAnErrorStartingRecording_andBackgroundRecordingRequested_displaysError() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(false); // FormController is not set
        MutableLiveData<Boolean> formControllerSet = new MutableLiveData<>(false);
        when(formEntryViewModel.isFormControllerSet()).thenReturn(formControllerSet);

        audioRecorder.cleanUp(); // Reset recorder

        FragmentScenario<AudioRecordingControllerFragment> scenario = FragmentScenario.launch(AudioRecordingControllerFragment.class);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(true); // FormController is set
        formControllerSet.postValue(true);

        audioRecorder.failOnStart();
        audioRecorder.start("background", Output.AAC);
        scenario.onFragment(fragment -> {
            assertThat(fragment.binding.getRoot().getVisibility(), is(View.VISIBLE));
            assertThat(fragment.binding.recordingStatusMessage.getText(), is("Could not start recording."));
        });
    }
}
