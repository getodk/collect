package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.utilities.Clock;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.FormEntryViewModel.NonFatal;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class FormEntryViewModelTest {

    private FormEntryViewModel viewModel;
    private FormController formController;
    private FormIndex startingIndex;
    private AuditEventLogger auditEventLogger;
    private Clock clock;
    private FakeRecordAudioActionRegistry recordAudioActionRegistry;
    private AudioRecorder audioRecorder;
    private PermissionsChecker permissionsChecker;

    @Before
    public void setup() {
        formController = mock(FormController.class);
        startingIndex = new FormIndex(null, 0, 0, new TreeReference());
        when(formController.getFormIndex()).thenReturn(startingIndex);
        when(formController.getCurrentFormIdentifierHash()).thenReturn("formIdentifierHash");

        auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);

        clock = mock(Clock.class);

        audioRecorder = mock(AudioRecorder.class);
        recordAudioActionRegistry = new FakeRecordAudioActionRegistry();

        permissionsChecker = mock(PermissionsChecker.class);
        viewModel = new FormEntryViewModel(clock, mock(Analytics.class), new PreferencesProvider(ApplicationProvider.getApplicationContext()), audioRecorder, permissionsChecker, recordAudioActionRegistry);
        viewModel.formLoaded(formController);
    }

    @Test
    public void addRepeat_stepsToNextScreenEvent() throws Exception {
        viewModel.addRepeat();
        verify(formController).stepToNextScreenEvent();
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithMessage() {
        doThrow(new RuntimeException(new IOException("OH NO"))).when(formController).newRepeat();

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorCreatingRepeat_setsErrorWithoutCause() {
        RuntimeException runtimeException = mock(RuntimeException.class);
        when(runtimeException.getCause()).thenReturn(null);
        when(runtimeException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");

        doThrow(runtimeException).when(formController).newRepeat();

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("Unknown issue occurred while adding a new group")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void addRepeat_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithoutCause() throws Exception {
        JavaRosaException javaRosaException = mock(JavaRosaException.class);
        when(javaRosaException.getCause()).thenReturn(null);
        when(javaRosaException.getMessage()).thenReturn("Unknown issue occurred while adding a new group");

        when(formController.stepToNextScreenEvent()).thenThrow(javaRosaException);

        viewModel.addRepeat();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("Unknown issue occurred while adding a new group")));
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndAddRepeat_doesNotJumpBack() {
        viewModel.promptForNewRepeat();
        viewModel.addRepeat();

        viewModel.cancelRepeatPrompt();
        verify(formController, never()).jumpToIndex(startingIndex);
    }

    @Test
    public void cancelRepeatPrompt_afterPromptForNewRepeatAndCancelRepeatPrompt_doesNotJumpBack() {
        viewModel.promptForNewRepeat();
        viewModel.cancelRepeatPrompt();
        verify(formController).jumpToIndex(startingIndex);

        viewModel.cancelRepeatPrompt();
        verify(formController, atMostOnce()).jumpToIndex(startingIndex);
    }

    @Test
    public void cancelRepeatPrompt_whenThereIsAnErrorSteppingToNextScreen_setsErrorWithMessage() throws Exception {
        when(formController.stepToNextScreenEvent()).thenThrow(new JavaRosaException(new IOException("OH NO")));

        viewModel.cancelRepeatPrompt();
        assertThat(viewModel.getError().getValue(), equalTo(new NonFatal("OH NO")));
    }

    @Test
    public void openHierarchy_logsHierarchyAuditEvent() {
        when(clock.getCurrentTime()).thenReturn(12345L);
        viewModel.openHierarchy();
        verify(auditEventLogger).logEvent(AuditEvent.AuditEventType.HIERARCHY, true, 12345L);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsVoiceOnly_startsAMRRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, "voice-only");

        verify(audioRecorder).start(new HashSet<TreeReference>() {{
            add(treeReference);
        }}, Output.AMR);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsLow_startsAACLowRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, "low");

        verify(audioRecorder).start(new HashSet<TreeReference>() {{
            add(treeReference);
        }}, Output.AAC_LOW);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsMissings_startsAMRRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, null);

        verify(audioRecorder).start(new HashSet<TreeReference>() {{
            add(treeReference);
        }}, Output.AMR);
    }

    @Test
    public void grantAudioPermission_startsBackgroundRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(false);

        TreeReference treeReference1 = new TreeReference();
        TreeReference treeReference2 = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference1, "low");
        recordAudioActionRegistry.listener.accept(treeReference2, "low");

        viewModel.grantAudioPermission();
        verify(audioRecorder).start(new HashSet<TreeReference>() {{
            add(treeReference1);
            add(treeReference2);
        }}, Output.AAC_LOW);
    }

    @Test
    public void grantAudioPermission_clearsErrror() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(false);

        TreeReference treeReference1 = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference1, "low");

        viewModel.grantAudioPermission();
        assertThat(viewModel.getError().getValue(), is(nullValue()));
    }

    @Test
    public void onCleared_unregistersRecordAudioActionListener() {
        viewModel.onCleared();
        assertThat(recordAudioActionRegistry.listener, is(nullValue()));
    }

    private static class FakeRecordAudioActionRegistry implements FormEntryViewModel.RecordAudioActionRegistry {


        private BiConsumer<TreeReference, String> listener;

        @Override
        public void register(BiConsumer<TreeReference, String> listener) {
            this.listener = listener;
        }

        @Override
        public void unregister() {
            this.listener = null;
        }
    }
}
