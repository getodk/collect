package org.odk.collect.android.widgets.utilities;

import androidx.activity.ComponentActivity;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormIndexAnimationHandler;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.RecordingSession;

public class InternalRecordingRequester implements RecordingRequester {

    private static FormIndex formIndex;

    private final ComponentActivity activity;
    private final AudioRecorderViewModel audioRecorderViewModel;
    private final PermissionsProvider permissionsProvider;
    private final FormEntryViewModel formEntryViewModel;
    private final FormSaveViewModel formSaveViewModel;
    private final FormIndexAnimationHandler.Listener refreshListener;

    public InternalRecordingRequester(ComponentActivity activity, AudioRecorderViewModel audioRecorderViewModel, PermissionsProvider permissionsProvider, FormEntryViewModel formEntryViewModel, FormSaveViewModel formSaveViewModel, FormIndexAnimationHandler.Listener refreshListener) {
        this.activity = activity;
        this.audioRecorderViewModel = audioRecorderViewModel;
        this.permissionsProvider = permissionsProvider;
        this.formEntryViewModel = formEntryViewModel;
        this.formSaveViewModel = formSaveViewModel;
        this.refreshListener = refreshListener;

        audioRecorderViewModel.getCurrentSession().observe(activity, session -> {
            if (session != null && session.getFile() != null) {
                handleRecording(session);
            }
        });
    }

    private void handleRecording(RecordingSession session) {
        formSaveViewModel.createAnswerFile(session.getFile()).observe(activity, result -> {
            if (result != null) {
                if (result.isSuccess() && !session.getId().equals("background")) {
                    session.getFile().delete();
                }

                audioRecorderViewModel.cleanUp();

                try {
                    FormIndex formIndex = InternalRecordingRequester.formIndex;
                    if (formIndex != null) {
                        formSaveViewModel.replaceAnswerFile(formIndex.toString(), result.getOrNull().getAbsolutePath());
                        Collect.getInstance().getFormController().answerQuestion(formIndex, new StringData(result.getOrNull().getName()));
                        refreshListener.onScreenRefresh();
                    }
                } catch (JavaRosaException e) {
                    // ?
                }
            }
        });
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                String quality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");
                if (quality != null && quality.equals("voice-only")) {
                    audioRecorderViewModel.start(prompt.getIndex().toString(), Output.AMR);
                } else if (quality != null && quality.equals("low")) {
                    audioRecorderViewModel.start(prompt.getIndex().toString(), Output.AAC_LOW);
                } else {
                    audioRecorderViewModel.start(prompt.getIndex().toString(), Output.AAC);
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
