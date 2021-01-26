package org.odk.collect.android.widgets.utilities;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

public class RecordingRequesterFactory {

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final ActivityAvailability activityAvailability;
    private final PermissionsProvider permissionsProvider;
    private final ComponentActivity activity;
    private final LifecycleOwner lifecycle;
    private final AudioRecorderViewModel audioRecorderViewModel;
    private final FormEntryViewModel formEntryViewModel;

    public RecordingRequesterFactory(WaitingForDataRegistry waitingForDataRegistry, QuestionMediaManager questionMediaManager, ActivityAvailability activityAvailability, AudioRecorderViewModel audioRecorderViewModel, PermissionsProvider permissionsProvider, ComponentActivity activity, LifecycleOwner lifecycle, FormEntryViewModel formEntryViewModel) {
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.activityAvailability = activityAvailability;
        this.audioRecorderViewModel = audioRecorderViewModel;
        this.permissionsProvider = permissionsProvider;
        this.activity = activity;
        this.lifecycle = lifecycle;
        this.formEntryViewModel = formEntryViewModel;
    }

    public RecordingRequester create(FormEntryPrompt prompt, boolean externalRecorderPreferred) {
        String audioQuality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");

        if (audioQuality != null && (audioQuality.equals("normal") || audioQuality.equals("voice-only") || audioQuality.equals("low"))) {
            return new InternalRecordingRequester(activity, audioRecorderViewModel, permissionsProvider, lifecycle, questionMediaManager, formEntryViewModel);
        } else if (audioQuality != null && audioQuality.equals("external")) {
            return new ExternalAppRecordingRequester(activity, activityAvailability, waitingForDataRegistry, permissionsProvider, formEntryViewModel, audioRecorderViewModel, lifecycle);
        } else if (externalRecorderPreferred) {
            return new ExternalAppRecordingRequester(activity, activityAvailability, waitingForDataRegistry, permissionsProvider, formEntryViewModel, audioRecorderViewModel, lifecycle);
        } else {
            return new InternalRecordingRequester(activity, audioRecorderViewModel, permissionsProvider, lifecycle, questionMediaManager, formEntryViewModel);
        }
    }
}
