package org.odk.collect.android.formentry;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioFileAppender;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.utilities.Result;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import timber.log.Timber;

public class RecordingHandler {

    private final QuestionMediaManager questionMediaManager;
    private final LifecycleOwner lifecycleOwner;
    private final AudioRecorder audioRecorder;
    private final AudioFileAppender audioFileAppender;

    public RecordingHandler(QuestionMediaManager questionMediaManager, LifecycleOwner lifecycleOwner, AudioRecorder audioRecorder, AudioFileAppender audioFileAppender) {
        this.questionMediaManager = questionMediaManager;
        this.lifecycleOwner = lifecycleOwner;
        this.audioRecorder = audioRecorder;
        this.audioFileAppender = audioFileAppender;
    }

    public void handle(FormController formController, RecordingSession session, Runnable onRecordingHandled) {
        questionMediaManager.createAnswerFile(session.getFile()).observe(lifecycleOwner, result -> {
            if (result != null && result.isSuccess()) {
                audioRecorder.cleanUp();

                try {
                    if (session.getId() instanceof FormIndex) {
                        handleForegroundRecording(formController, session, result);
                    } else if (session.getId() instanceof HashSet) {
                        handleBackgroundRecording(formController, session, result);
                    }

                    onRecordingHandled.run();
                } catch (JavaRosaException | IOException e) {
                    Timber.e(e);
                    ToastUtils.showLongToast(R.string.saving_audio_recording_failed);
                }
            }
        });
    }

    private void handleForegroundRecording(FormController formController, RecordingSession session, Result<File> result) throws JavaRosaException {
        FormIndex formIndex = (FormIndex) session.getId();
        questionMediaManager.replaceAnswerFile(formIndex.toString(), result.getOrNull().getAbsolutePath());
        formController.answerQuestion(formIndex, new StringData(result.getOrNull().getName()));
        session.getFile().delete();
    }

    private void handleBackgroundRecording(FormController formController, RecordingSession session, Result<File> result) throws IOException {
        HashSet<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();

        TreeReference firstReference = treeReferences.iterator().next();
        StringData answer = (StringData) formController.getAnswer(firstReference);

        if (answer != null) {
            audioFileAppender.append(questionMediaManager.getAnswerFile((String) answer.getValue()), result.getOrNull());
            result.getOrNull().delete();
        } else {
            for (TreeReference treeReference : treeReferences) {
                formController.getFormDef().setValue(new StringData(result.getOrNull().getName()), treeReference, false);
            }
        }

        session.getFile().delete();
    }
}
