package org.odk.collect.android.widgets.utilities;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;

import java.util.Optional;

public class RecordingRequesterFactory {

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final ActivityAvailability activityAvailability;
    private final AudioRecorderViewModelFactory audioRecorderViewModelFactory;
    private final PermissionUtils permissionUtils;

    public RecordingRequesterFactory(WaitingForDataRegistry waitingForDataRegistry, QuestionMediaManager questionMediaManager, ActivityAvailability activityAvailability, AudioRecorderViewModelFactory audioRecorderViewModelFactory, PermissionUtils permissionUtils) {
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.activityAvailability = activityAvailability;
        this.audioRecorderViewModelFactory = audioRecorderViewModelFactory;
        this.permissionUtils = permissionUtils;
    }

    public RecordingRequester create(FormEntryPrompt prompt, boolean externalRecorderPreferred, ComponentActivity activity, LifecycleOwner viewLifecycle) {
        RecordingRequester recordingRequester;

        Optional<String> audioQuality = FormEntryPromptUtils.getAttributeValue(prompt, "audio-quality");
        if (audioQuality.isPresent() && audioQuality.get().equals("external")) {
            recordingRequester = new ExternalAppRecordingRequester(activity, activityAvailability, waitingForDataRegistry, permissionUtils);
        } else if (externalRecorderPreferred) {
            recordingRequester = new ExternalAppRecordingRequester(activity, activityAvailability, waitingForDataRegistry, permissionUtils);
        } else {
            AudioRecorderViewModel viewModel = new ViewModelProvider(activity, audioRecorderViewModelFactory).get(AudioRecorderViewModel.class);
            recordingRequester = new InternalRecordingRequester(activity, viewModel, permissionUtils, viewLifecycle, questionMediaManager);
        }
        return recordingRequester;
    }
}
