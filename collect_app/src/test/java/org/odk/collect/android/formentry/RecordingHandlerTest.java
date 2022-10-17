package org.odk.collect.android.formentry;


import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
    private final FormDef formDef = mock(FormDef.class);
    private final AudioFileAppender amrAppender = mock(AudioFileAppender.class);
    private final AudioFileAppender m4aAppender = mock(AudioFileAppender.class);

    RecordingHandler recordingHandler;

    @Before
    public void setup() {
        recordingHandler = new RecordingHandler(questionMediaManager, new FakeLifecycleOwner(), mock(AudioRecorder.class), amrAppender, m4aAppender);
        doReturn(formDef).when(formController).getFormDef();
    }

    @Test
    public void whenBackgroundRecordingM4A_andThereIsNoRecordingAlreadySavedForReference_savesNewAnswer() throws Exception {
        TreeReference treeReference = new TreeReference();

        File newRecording = File.createTempFile("new", ".m4a");
        RecordingSession recordingSession = new RecordingSession(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, newRecording, 0, 0, false);

        recordingHandler.handle(formController, recordingSession, success -> {
        });

        ArgumentCaptor<StringData> dataAc = ArgumentCaptor.forClass(StringData.class);
        ArgumentCaptor<TreeReference> refAc = ArgumentCaptor.forClass(TreeReference.class);
        ArgumentCaptor<Boolean> midSurveyAc = ArgumentCaptor.forClass(Boolean.class);

        verify(formDef).setValue(dataAc.capture(), refAc.capture(), midSurveyAc.capture());
        assertThat(dataAc.getValue(), is(new StringData(questionMediaManager.getAnswerFile(newRecording.getName()).getName())));
        assertThat(refAc.getValue(), is(treeReference));
        assertFalse(midSurveyAc.getValue());

        verifyNoInteractions(m4aAppender);
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
        verifyNoInteractions(formDef);
    }

    @Test
    public void whenBackgroundRecordingM4A_andRecordingAlreadySavedForReferenceButTheAudioFileDoesNotExist_savesNewAnswer() throws Exception {
        File recording = File.createTempFile("existing", ".m4a");
        File existingRecording = questionMediaManager.createAnswerFile(recording).getValue().getOrNull();
        assertThat(existingRecording, not(nullValue()));

        existingRecording.delete();

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

        ArgumentCaptor<StringData> dataAc = ArgumentCaptor.forClass(StringData.class);
        ArgumentCaptor<TreeReference> refAc = ArgumentCaptor.forClass(TreeReference.class);
        ArgumentCaptor<Boolean> midSurveyAc = ArgumentCaptor.forClass(Boolean.class);

        verify(formDef).setValue(dataAc.capture(), refAc.capture(), midSurveyAc.capture());
        assertThat(dataAc.getValue(), is(new StringData(questionMediaManager.getAnswerFile(newRecording.getName()).getName())));
        assertThat(refAc.getValue(), is(treeReference));
        assertFalse(midSurveyAc.getValue());

        verifyNoInteractions(m4aAppender);
    }

    @Test
    public void whenBackgroundRecordingAMR_andThereIsNoRecordingAlreadySavedForReference_savesNewAnswer() throws Exception {
        TreeReference treeReference = new TreeReference();

        File newRecording = File.createTempFile("new", ".amr");
        RecordingSession recordingSession = new RecordingSession(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, newRecording, 0, 0, false);

        recordingHandler.handle(formController, recordingSession, success -> {
        });

        ArgumentCaptor<StringData> dataAc = ArgumentCaptor.forClass(StringData.class);
        ArgumentCaptor<TreeReference> refAc = ArgumentCaptor.forClass(TreeReference.class);
        ArgumentCaptor<Boolean> midSurveyAc = ArgumentCaptor.forClass(Boolean.class);

        verify(formDef).setValue(dataAc.capture(), refAc.capture(), midSurveyAc.capture());
        assertThat(dataAc.getValue(), is(new StringData(questionMediaManager.getAnswerFile(newRecording.getName()).getName())));
        assertThat(refAc.getValue(), is(treeReference));
        assertFalse(midSurveyAc.getValue());

        verifyNoInteractions(amrAppender);
    }

    @Test
    public void whenBackgroundRecordingAMR_andRecordingAlreadySavedForReference_appendsAudioFiles() throws Exception {
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
        verifyNoInteractions(formDef);
    }

    @Test
    public void whenBackgroundRecordingAMR_andRecordingAlreadySavedForReferenceButTheAudioFileDoesNotExist_savesNewAnswer() throws Exception {
        File recording = File.createTempFile("existing", ".amr");
        File existingRecording = questionMediaManager.createAnswerFile(recording).getValue().getOrNull();
        assertThat(existingRecording, not(nullValue()));

        existingRecording.delete();

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

        ArgumentCaptor<StringData> dataAc = ArgumentCaptor.forClass(StringData.class);
        ArgumentCaptor<TreeReference> refAc = ArgumentCaptor.forClass(TreeReference.class);
        ArgumentCaptor<Boolean> midSurveyAc = ArgumentCaptor.forClass(Boolean.class);

        verify(formDef).setValue(dataAc.capture(), refAc.capture(), midSurveyAc.capture());
        assertThat(dataAc.getValue(), is(new StringData(questionMediaManager.getAnswerFile(newRecording.getName()).getName())));
        assertThat(refAc.getValue(), is(treeReference));
        assertFalse(midSurveyAc.getValue());

        verifyNoInteractions(amrAppender);
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
