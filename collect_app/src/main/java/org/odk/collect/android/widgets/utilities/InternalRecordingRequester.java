package org.odk.collect.android.widgets.utilities;

import static org.odk.collect.android.widgets.utilities.BindAttributes.QUALITY;

import androidx.activity.ComponentActivity;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.permissions.PermissionsProvider;

public class InternalRecordingRequester implements RecordingRequester {

    private final ComponentActivity activity;
    private final AudioRecorder audioRecorder;
    private final PermissionsProvider permissionsProvider;

    public InternalRecordingRequester(ComponentActivity activity, AudioRecorder audioRecorder, PermissionsProvider permissionsProvider) {
        this.activity = activity;
        this.audioRecorder = audioRecorder;
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, () -> {
            String quality = FormEntryPromptUtils.getBindAttribute(prompt, QUALITY);
            if (quality != null && quality.equals(BindAttributes.Quality.VOICE_ONLY.getValue())) {
                audioRecorder.start(prompt.getIndex(), Output.AMR);
            } else if (quality != null && quality.equals(BindAttributes.Quality.LOW.getValue())) {
                audioRecorder.start(prompt.getIndex(), Output.AAC_LOW);
            } else {
                audioRecorder.start(prompt.getIndex(), Output.AAC);
            }
        });
    }
}
