package org.odk.collect.android.widgets.utilities;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.robolectric.Robolectric;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;

@RunWith(AndroidJUnit4.class)
public class InternalRecordingRequesterTest {

    private final FakePermissionsProvider permissionsProvider = new FakePermissionsProvider();
    private final AudioRecorder audioRecorder = mock(AudioRecorder.class);

    private InternalRecordingRequester requester;

    @Before
    public void setup() {
        ComponentActivity activity = Robolectric.buildActivity(ComponentActivity.class).get();
        when(audioRecorder.getCurrentSession()).thenReturn(new MutableLiveData<>(null));

        requester = new InternalRecordingRequester(activity, audioRecorder, permissionsProvider);
        permissionsProvider.setPermissionGranted(true);
    }

    @Test
    public void requestRecording_startsWithAAC() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(audioRecorder).start(prompt.getIndex(), Output.AAC);
    }

    @Test
    public void requestRecording_whenPromptQualityIsVoiceOnly_startsWithAMR() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "voice-only")
                .build();

        requester.requestRecording(prompt);

        verify(audioRecorder).start(prompt.getIndex(), Output.AMR);
    }

    @Test
    public void requestRecording_whenPromptQualityIsLow_startsWithAACLow() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "low")
                .build();

        requester.requestRecording(prompt);

        verify(audioRecorder).start(prompt.getIndex(), Output.AAC_LOW);
    }

    @Test
    public void requestRecording_whenPermissionDenied_doesNothing() {
        permissionsProvider.setPermissionGranted(false);

        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(audioRecorder, never()).start(any(), any());
    }
}
