package org.odk.collect.android.widgets.utilities;

import android.app.Activity;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;

@RunWith(AndroidJUnit4.class)
public class InternalRecordingRequesterTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final AudioRecorderViewModel viewModel = mock(AudioRecorderViewModel.class);

    private InternalRecordingRequester requester;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).get();
        requester = new InternalRecordingRequester(activity, viewModel, permissionUtils, new FakeWaitingForDataRegistry());
        permissionUtils.setPermissionGranted(true);
    }

    @Test
    public void requestRecording_callsStartOnViewModel() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel).start();
    }

    @Test
    public void requestRecording_whenPermissionDenied_doesNothing() {
        permissionUtils.setPermissionGranted(false);

        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel, never()).start();
    }
}
