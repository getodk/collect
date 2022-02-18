package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.androidtest.FakeLifecycleOwner;

import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;

@RunWith(AndroidJUnit4.class)
public class AudioRecorderRecordingStatusHandlerTest {

    private final AudioRecorder audioRecorder = mock(AudioRecorder.class);
    private final FormEntryViewModel formEntryViewModel = mock(FormEntryViewModel.class);

    AudioRecorderRecordingStatusHandler provider;

    @Before
    public void setup() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(new MutableNonNullLiveData<>(false));
        provider = new AudioRecorderRecordingStatusHandler(audioRecorder, formEntryViewModel, new FakeLifecycleOwner());
    }

    @Test
    public void onIsRecordingChangedBlocked_listensToCurrentSession() {
        MutableLiveData<RecordingSession> liveData = new MutableLiveData<>(null);
        when(audioRecorder.getCurrentSession()).thenReturn(liveData);

        Consumer<Boolean> listener = mock(Consumer.class);
        provider.onBlockedStatusChange(listener);
        verify(listener).accept(false);

        liveData.setValue(new RecordingSession("blah", null, 0, 0, false));
        verify(listener).accept(true);
    }

    @Test
    public void onIsRecordingChangedBlocked_whenFormHasBackgroundAudio_isAlwaysTrue() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(new MutableNonNullLiveData<>(true));

        MutableLiveData<RecordingSession> liveData = new MutableLiveData<>(null);
        when(audioRecorder.getCurrentSession()).thenReturn(liveData);

        Consumer<Boolean> listener = mock(Consumer.class);
        provider.onBlockedStatusChange(listener);
        verify(listener).accept(true);
    }

    @Test
    public void whenViewModelSessionUpdates_callsInProgressListener() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        MutableLiveData<RecordingSession> sessionLiveData = new MutableLiveData<>(null);
        when(audioRecorder.getCurrentSession()).thenReturn(sessionLiveData);

        Consumer<Pair<Long, Integer>> listener = mock(Consumer.class);
        provider.onRecordingStatusChange(prompt, listener);
        verify(listener).accept(null);

        sessionLiveData.setValue(new RecordingSession(prompt.getIndex(), null, 1200L, 25, false));
        verify(listener).accept(new Pair<>(1200L, 25));
    }

    @Test
    public void whenViewModelSessionUpdates_forDifferentSession_callsInProgressListenerWithNull() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        MutableLiveData<RecordingSession> sessionLiveData = new MutableLiveData<>(null);
        when(audioRecorder.getCurrentSession()).thenReturn(sessionLiveData);

        Consumer<Pair<Long, Integer>> listener = mock(Consumer.class);
        provider.onRecordingStatusChange(prompt, listener);
        verify(listener).accept(null);

        sessionLiveData.setValue(new RecordingSession("something else", null, 1200L, 0, false));
        verify(listener, times(2)).accept(null);
    }
}
