package org.odk.collect.android.formentry;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.audio.AudioFileAppender;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.androidtest.FakeLifecycleOwner;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;

import java.io.File;
import java.util.HashSet;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class RecordingHandlerTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private final FakeQuestionMediaManager questionMediaManager = new FakeQuestionMediaManager();
    private final FormController formController = mock(FormController.class);
    private final AudioFileAppender amrAppender = mock(AudioFileAppender.class);
    private final AudioFileAppender m4aAppender = mock(AudioFileAppender.class);

    RecordingHandler recordingHandler;

    @Before
    public void setup() {
        recordingHandler = new RecordingHandler(questionMediaManager, new FakeLifecycleOwner(), mock(AudioRecorder.class), amrAppender, m4aAppender);
    }

    @Test
    public void whenBackgroundRecordingM4A_andRecordingAlreadySavedForReference_appendsAudioFiles() throws Exception {
        File recording = File.createTempFile("existing", ".m4a");
        File existingRecording = questionMediaManager.createAnswerFile(recording).getValue().getOrNull();
        assertThat(existingRecording, not(nullValue()));

        TreeReference treeReference = new TreeReference();
        when(formController.getAnswer(treeReference)).thenReturn(new StringData(existingRecording.getName()));

        File newRecording = File.createTempFile("new", ".m4a");
        RecordingSession recordingSession = new RecordingSession(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, newRecording, 0, 0, false);

        recordingHandler.handle(formController, recordingSession, success -> {
        });

        verify(m4aAppender).append(existingRecording, questionMediaManager.getAnswerFile(newRecording.getName()));
    }

    @Test
    public void whenBackgroundRecordinAMR_andRecordingAlreadySavedForReference_appendsAudioFiles() throws Exception {
        File recording = File.createTempFile("existing", ".amr");
        File existingRecording = questionMediaManager.createAnswerFile(recording).getValue().getOrNull();
        assertThat(existingRecording, not(nullValue()));

        TreeReference treeReference = new TreeReference();
        when(formController.getAnswer(treeReference)).thenReturn(new StringData(existingRecording.getName()));

        File newRecording = File.createTempFile("new", ".amr");
        RecordingSession recordingSession = new RecordingSession(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, newRecording, 0, 0, false);

        recordingHandler.handle(formController, recordingSession, success -> {
        });

        verify(amrAppender).append(existingRecording, questionMediaManager.getAnswerFile(newRecording.getName()));
    }

    @Test
    public void whenBackgroundRecording_andRecordingAlreadySavedForReference_deletesNewFile() throws Exception {
        File recording = File.createTempFile("existing", ".m4a");
        File existingRecording = questionMediaManager.createAnswerFile(recording).getValue().getOrNull();
        assertThat(existingRecording, not(nullValue()));

        TreeReference treeReference = new TreeReference();
        when(formController.getAnswer(treeReference)).thenReturn(new StringData(existingRecording.getName()));

        File newRecording = File.createTempFile("new", ".m4a");
        RecordingSession recordingSession = new RecordingSession(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, newRecording, 0, 0, false);

        recordingHandler.handle(formController, recordingSession, success -> {
        });

        assertThat(questionMediaManager.getAnswerFile(newRecording.getName()).exists(), is(false));
    }
}
