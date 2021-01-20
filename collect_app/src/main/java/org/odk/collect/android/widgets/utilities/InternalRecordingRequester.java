package org.odk.collect.android.widgets.utilities;

import android.app.Activity;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

public class InternalRecordingRequester implements RecordingRequester {

    public static FormIndex formIndex;

    private final Activity activity;
    private final AudioRecorderViewModel viewModel;
    private final PermissionsProvider permissionsProvider;
    private final FormEntryViewModel formEntryViewModel;

    public InternalRecordingRequester(Activity activity, AudioRecorderViewModel viewModel, PermissionsProvider permissionsProvider, FormEntryViewModel formEntryViewModel) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.permissionsProvider = permissionsProvider;
        this.formEntryViewModel = formEntryViewModel;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                String quality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");
                if (quality != null && quality.equals("voice-only")) {
                    viewModel.start(prompt.getIndex().toString(), Output.AMR);
                } else if (quality != null && quality.equals("low")) {
                    viewModel.start(prompt.getIndex().toString(), Output.AAC_LOW);
                } else {
                    viewModel.start(prompt.getIndex().toString(), Output.AAC);
                }

                formIndex = prompt.getIndex();
            }

            @Override
            public void denied() {

            }
        });

        formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_INTERNAL);
    }
}
