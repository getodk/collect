package org.odk.collect.android.widgets.utilities;

import android.app.Activity;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

public class InternalRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final AudioRecorderViewModel viewModel;
    private final PermissionUtils permissionUtils;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public InternalRecordingRequester(Activity activity, AudioRecorderViewModel viewModel, PermissionUtils permissionUtils, WaitingForDataRegistry waitingForDataRegistry) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.permissionUtils = permissionUtils;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionUtils.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                viewModel.start();
            }

            @Override
            public void denied() {

            }
        });
    }
}
