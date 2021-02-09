package org.odk.collect.android.formentry;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.utilities.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class RecordingHandler {

    private final QuestionMediaManager questionMediaManager;
    private final LifecycleOwner lifecycleOwner;
    private final AudioRecorder audioRecorder;

    public RecordingHandler(FormSaveViewModel questionMediaManager, LifecycleOwner lifecycleOwner, AudioRecorder audioRecorder) {
        this.questionMediaManager = questionMediaManager;
        this.lifecycleOwner = lifecycleOwner;
        this.audioRecorder = audioRecorder;
    }

    public void handle(RecordingSession session, Runnable onRecordingHandled) {
        questionMediaManager.createAnswerFile(session.getFile()).observe(lifecycleOwner, result -> {
            if (result != null && result.isSuccess()) {
                audioRecorder.cleanUp();

                try {
                    if (session.getId() instanceof FormIndex) {
                        handleForegroundRecording(session, result);
                    } else if (session.getId() instanceof Set) {
                        handleBackgroundRecording(session, result);
                    }

                    onRecordingHandled.run();
                } catch (JavaRosaException e) {
                    Timber.e(e);
                    ToastUtils.showLongToast(R.string.saving_audio_recording_failed);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleForegroundRecording(RecordingSession session, Result<File> result) throws JavaRosaException {
        FormIndex formIndex = (FormIndex) session.getId();
        questionMediaManager.replaceAnswerFile(formIndex.toString(), result.getOrNull().getAbsolutePath());
        Collect.getInstance().getFormController().answerQuestion(formIndex, new StringData(result.getOrNull().getName()));
        session.getFile().delete();
    }

    private void handleBackgroundRecording(RecordingSession session, Result<File> result) throws Exception {
        Set<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();

        TreeReference firstReference = treeReferences.iterator().next();
        TreeElement treeElement = Collect.getInstance().getFormController().getFormDef().getMainInstance().resolveReference(firstReference);

        if (treeElement.getValue() != null) {
            combineFiles(questionMediaManager.getAnswerFile((String) treeElement.getValue().getValue()), result.getOrNull());
            result.getOrNull().delete();
        } else {
            for (TreeReference treeReference : treeReferences) {
                Collect.getInstance().getFormController().getFormDef().setValue(new StringData(result.getOrNull().getName()), treeReference, false);
            }
        }

        session.getFile().delete();
    }

    private void combineFiles(File existingFile, File newFile) throws Exception {
        if (existingFile.getName().endsWith(".m4a")) {
            combineMP4Files(existingFile, newFile);
        } else if (existingFile.getName().endsWith(".amr")) {
            combineAMRFiles(existingFile, newFile);
        } else {
            throw new IllegalArgumentException("Using unknown container for recording!");
        }
    }

    private void combineMP4Files(File existingFile, File newFile) throws IOException {
        Track existingTrack = MovieCreator.build(existingFile.getAbsolutePath()).getTracks().get(0);
        Track newTrack = MovieCreator.build(newFile.getAbsolutePath()).getTracks().get(0);

        Movie movie = new Movie();
        movie.addTrack(new AppendTrack(existingTrack, newTrack));
        Container container = new DefaultMp4Builder().build(movie);

        try (FileChannel fileChannel = new RandomAccessFile(existingFile, "rw").getChannel()) {
            container.writeContainer(fileChannel);
        }
    }

    private void combineAMRFiles(File outputFile, File inputFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFile, true);
        FileInputStream fis = new FileInputStream(inputFile);

        byte[] fileContent = new byte[(int) inputFile.length()];
        fis.read(fileContent);

        byte[] headerlessFileContent = new byte[fileContent.length - 6];
        if (fileContent.length - 6 >= 0) {
            System.arraycopy(fileContent, 6, headerlessFileContent, 0, fileContent.length - 6);
        }

        fileContent = headerlessFileContent;
        fos.write(fileContent);
    }
}
