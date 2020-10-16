package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;

public class GetContentAudioFileRequester implements AudioFileRequester {

    private final Activity activity;
    private final ActivityAvailability activityAvailability;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public GetContentAudioFileRequester(Activity activity, ActivityAvailability activityAvailability, WaitingForDataRegistry waitingForDataRegistry) {
        this.activity = activity;
        this.activityAvailability = activityAvailability;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public void requestFile(FormEntryPrompt prompt) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        if (activityAvailability.isActivityAvailable(intent)) {
            waitingForDataRegistry.waitForData(prompt.getIndex());
            activity.startActivityForResult(intent, ApplicationConstants.RequestCodes.AUDIO_CHOOSER);
        } else {
            Toast.makeText(activity, activity.getString(R.string.activity_not_found, activity.getString(R.string.choose_audio)), Toast.LENGTH_SHORT).show();
            waitingForDataRegistry.cancelWaitingForData();
        }
    }
}
