package org.odk.collect.android.widgets.utilities;

import android.app.Activity;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.Output;

import java.util.function.Consumer;

public class InternalRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final AudioRecorderViewModel viewModel;
    private final PermissionUtils permissionUtils;
    private final LifecycleOwner lifecycleOwner;
    private final QuestionMediaManager questionMediaManager;

    public InternalRecordingRequester(Activity activity, AudioRecorderViewModel viewModel, PermissionUtils permissionUtils, LifecycleOwner lifecycleOwner, QuestionMediaManager questionMediaManager) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.permissionUtils = permissionUtils;
        this.lifecycleOwner = lifecycleOwner;
        this.questionMediaManager = questionMediaManager;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionUtils.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                String quality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");
                if (quality != null && quality.equals("voice-only")) {
                    viewModel.start(prompt.getIndex().toString(), Output.AMR);
                } else {
                    viewModel.start(prompt.getIndex().toString(), Output.AAC);
                }
            }

            @Override
            public void denied() {

            }
        });
    }

    @Override
    public void onIsRecordingChanged(Consumer<Boolean> isRecordingListener) {
        viewModel.isRecording().observe(lifecycleOwner, isRecordingListener::accept);
    }

    @Override
    public void onRecordingAvailable(FormEntryPrompt prompt, Consumer<String> recordingAvailableListener) {
        viewModel.getRecording(prompt.getIndex().toString()).observe(lifecycleOwner, file -> {
            if (file != null) {
                questionMediaManager.createAnswerFile(file).observe(lifecycleOwner, fileName -> {
                    if (fileName != null) {
                        viewModel.cleanUp();
                        recordingAvailableListener.accept(fileName);
                    }
                });
            }
        });
    }
}
