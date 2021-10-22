package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.androidshared.utils.IntentLauncher;

public class ExternalAppRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final PermissionsProvider permissionsProvider;
    private final IntentLauncher intentLauncher;
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final FormEntryViewModel formEntryViewModel;

    public ExternalAppRecordingRequester(Activity activity, IntentLauncher intentLauncher, WaitingForDataRegistry waitingForDataRegistry, PermissionsProvider permissionsProvider, FormEntryViewModel formEntryViewModel) {
        this.activity = activity;
        this.permissionsProvider = permissionsProvider;
        this.intentLauncher = intentLauncher;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.formEntryViewModel = formEntryViewModel;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());

                waitingForDataRegistry.waitForData(prompt.getIndex());
                intentLauncher.launchForResult(activity, intent, ApplicationConstants.RequestCodes.AUDIO_CAPTURE, () -> {
                    Toast.makeText(activity, activity.getString(R.string.activity_not_found,
                            activity.getString(R.string.capture_audio)), Toast.LENGTH_SHORT).show();
                    waitingForDataRegistry.cancelWaitingForData();
                    return null;
                });
            }

            @Override
            public void denied() {
            }
        });

        formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_EXTERNAL);
    }
}
