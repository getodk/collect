package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.PermissionUtils;

public class ExternalAppAudioDataRequester implements AudioDataRequester {

    private final Activity activity;
    private final PermissionUtils permissionUtils;
    private final ActivityAvailability activityAvailability;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public ExternalAppAudioDataRequester(Activity activity, ActivityAvailability activityAvailability, WaitingForDataRegistry waitingForDataRegistry, PermissionUtils permissionUtils) {
        this.activity = activity;
        this.permissionUtils = permissionUtils;
        this.activityAvailability = activityAvailability;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionUtils.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());

                if (activityAvailability.isActivityAvailable(intent)) {
                    waitingForDataRegistry.waitForData(prompt.getIndex());
                    activity.startActivityForResult(intent, ApplicationConstants.RequestCodes.AUDIO_CAPTURE);
                } else {
                    Toast.makeText(activity, activity.getString(R.string.activity_not_found,
                            activity.getString(R.string.capture_audio)), Toast.LENGTH_SHORT).show();
                    waitingForDataRegistry.cancelWaitingForData();
                }
            }

            @Override
            public void denied() {
            }
        });
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
