package org.odk.collect.android.widgets.utilities;

import android.app.Activity;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.testshared.FakeLifecycleOwner;
import org.robolectric.Robolectric;

import java.io.File;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;

@RunWith(AndroidJUnit4.class)
public class InternalRecordingRequesterTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final QuestionMediaManager questionMediaManager = mock(QuestionMediaManager.class);
    private final AudioRecorderViewModel viewModel = mock(AudioRecorderViewModel.class);

    private InternalRecordingRequester requester;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).get();
        requester = new InternalRecordingRequester(activity, viewModel, permissionUtils, new FakeLifecycleOwner(), questionMediaManager);
        permissionUtils.setPermissionGranted(true);
    }

    @Test
    public void requestRecording_callsStartOnViewModel() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel).start(prompt.getIndex().toString());
    }

    @Test
    public void requestRecording_whenPermissionDenied_doesNothing() {
        permissionUtils.setPermissionGranted(false);

        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel, never()).start(any());
    }

    @Test
    public void onIsRecordingChanged_listensToIsRecording() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>(false);
        when(viewModel.isRecording()).thenReturn(liveData);

        Consumer<Boolean> listener = mock(Consumer.class);
        requester.onIsRecordingChanged(listener);
        verify(listener).accept(false);

        liveData.setValue(true);
        verify(listener).accept(false);
    }

    @Test
    public void whenViewModelRecordingAvailable_copiesFileToInstanceFolder_andCallsListenerForSessionWithFilename_andCleansUpViewModel() throws Exception {
        FormEntryPrompt prompt = promptWithAnswer(null);
        File file = File.createTempFile("blah", ".mp3");
        MutableLiveData<File> recordingLiveData = new MutableLiveData<>(null);
        MutableLiveData<String> answerLiveData = new MutableLiveData<>(null);
        when(viewModel.getRecording(prompt.getIndex().toString())).thenReturn(recordingLiveData);
        when(questionMediaManager.createAnswerFile(file)).thenReturn(answerLiveData);

        Consumer<String> listener = mock(Consumer.class);
        requester.onRecordingAvailable(prompt, listener);
        recordingLiveData.setValue(file);
        answerLiveData.setValue("copiedFile");

        verify(listener).accept("copiedFile");
        verify(viewModel).cleanUp();
    }
}
