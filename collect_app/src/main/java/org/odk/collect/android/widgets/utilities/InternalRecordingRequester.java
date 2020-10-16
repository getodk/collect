package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorderActivity;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.INTERNAL_AUDIO_CAPTURE;

public class InternalRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public InternalRecordingRequester(Activity activity, WaitingForDataRegistry waitingForDataRegistry) {
        this.activity = activity;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        int appTheme = new ThemeUtils(activity).getAppTheme();
        Intent intent = new Intent(activity, AudioRecorderActivity.class);
        intent.putExtra(AudioRecorderActivity.ARGS.THEME, appTheme);

        waitingForDataRegistry.waitForData(prompt.getIndex());
        activity.startActivityForResult(intent, INTERNAL_AUDIO_CAPTURE);
    }
}
