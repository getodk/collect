package org.odk.collect.android.widgets.utilities;

import androidx.activity.ComponentActivity;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryPrompt;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormIndexAnimationHandler;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Set;

import timber.log.Timber;

public class InternalRecordingRequester implements RecordingRequester {

    private final ComponentActivity activity;
    private final AudioRecorder audioRecorder;
    private final PermissionsProvider permissionsProvider;
    private final FormEntryViewModel formEntryViewModel;
    private final FormSaveViewModel formSaveViewModel;
    private final FormIndexAnimationHandler.Listener refreshListener;

    public InternalRecordingRequester(ComponentActivity activity, AudioRecorder audioRecorder, PermissionsProvider permissionsProvider, FormEntryViewModel formEntryViewModel, FormSaveViewModel formSaveViewModel, FormIndexAnimationHandler.Listener refreshListener) {
        this.activity = activity;
        this.audioRecorder = audioRecorder;
        this.permissionsProvider = permissionsProvider;
        this.formEntryViewModel = formEntryViewModel;
        this.formSaveViewModel = formSaveViewModel;
        this.refreshListener = refreshListener;

        audioRecorder.getCurrentSession().observe(activity, session -> {
            if (session != null && session.getFile() != null) {
                handleRecording(session);
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
                    audioRecorder.start(prompt.getIndex(), Output.AMR);
                } else if (quality != null && quality.equals("low")) {
                    audioRecorder.start(prompt.getIndex(), Output.AAC_LOW);
                } else {
                    audioRecorder.start(prompt.getIndex(), Output.AAC);
                }
            }

            @Override
            public void denied() {

            }
        });

        formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_INTERNAL);
    }

    private void handleRecording(RecordingSession session) {
        formSaveViewModel.createAnswerFile(session.getFile()).observe(activity, result -> {
            if (result != null && result.isSuccess()) {
                audioRecorder.cleanUp();

                try {
                    if (session.getId() instanceof FormIndex) {
                        FormIndex formIndex = (FormIndex) session.getId();
                        formSaveViewModel.replaceAnswerFile(formIndex.toString(), result.getOrNull().getAbsolutePath());
                        Collect.getInstance().getFormController().answerQuestion(formIndex, new StringData(result.getOrNull().getName()));
                        refreshListener.onScreenRefresh();
                    } else if (session.getId() instanceof Set) {
                        Set<TreeReference> treeReferences = (Set<TreeReference>) session.getId();

                        TreeReference firstReference = treeReferences.iterator().next();
                        TreeElement treeElement = Collect.getInstance().getFormController().getFormDef().getMainInstance().resolveReference(firstReference);

                        if (treeElement.getValue() != null) {
                            combineFiles(formSaveViewModel.getAnswerFile((String) treeElement.getValue().getValue()), result.getOrNull());
                            result.getOrNull().delete();
                        } else {
                            for (TreeReference treeReference : treeReferences) {
                                Collect.getInstance().getFormController().getFormDef().setValue(new StringData(result.getOrNull().getName()), treeReference, false);
                            }
                        }
                    }

                    session.getFile().delete();
                } catch (JavaRosaException e) {
                    Timber.e(e);
                    ToastUtils.showLongToast(R.string.saving_audio_recording_failed);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                formSaveViewModel.resumeSave();
            }
        });
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
        FileOutputStream fos = new FileOutputStream(outputFile, true); // Second parameter to indicate appending of data
        FileInputStream fis = new FileInputStream(inputFile);

        byte fileContent[] = new byte[(int) inputFile.length()];
        fis.read(fileContent);// Reads the file content as byte from the list.

        /* copy the entire file, but not the first 6 bytes */
        byte[] headerlessFileContent = new byte[fileContent.length - 6];
        if (fileContent.length - 6 >= 0) {
            System.arraycopy(fileContent, 6, headerlessFileContent, 0, fileContent.length - 6);
        }

        fileContent = headerlessFileContent;

        /* Write the byte into the combine file. */
        fos.write(fileContent);
    }
}
