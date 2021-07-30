package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.shared.Settings;
import org.odk.collect.testshared.RobolectricHelpers;
import org.odk.collect.utilities.Clock;

import java.util.HashSet;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class BackgroundAudioViewModelTest {

    private final PermissionsChecker permissionsChecker = mock(PermissionsChecker.class);
    private final FakeRecordAudioActionRegistry recordAudioActionRegistry = new FakeRecordAudioActionRegistry();
    private final AudioRecorder audioRecorder = mock(AudioRecorder.class);

    private BackgroundAudioViewModel viewModel;
    private Clock clock;

    @Before
    public void setup() {
        clock = mock(Clock.class);

        Settings generalSettings = TestSettingsProvider.getGeneralSettings();
        generalSettings.clear();

        viewModel = new BackgroundAudioViewModel(audioRecorder, generalSettings, recordAudioActionRegistry, permissionsChecker, clock);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsVoiceOnly_startsAMRRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, "voice-only");
        RobolectricHelpers.runLooper();

        verify(audioRecorder).start(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, Output.AMR);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsLow_startsAACLowRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, "low");
        RobolectricHelpers.runLooper();

        verify(audioRecorder).start(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, Output.AAC_LOW);
    }

    @Test
    public void whenRecordAudioActionIsTriggered_whenQualityIsMissings_startsAMRRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(true);

        TreeReference treeReference = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference, null);
        RobolectricHelpers.runLooper();

        verify(audioRecorder).start(new HashSet<TreeReference>() {
            {
                add(treeReference);
            }
        }, Output.AMR);
    }

    @Test
    public void onCleared_unregistersRecordAudioActionListener() {
        viewModel.onCleared();
        assertThat(recordAudioActionRegistry.listener, is(nullValue()));
    }

    @Test
    public void grantAudioPermission_startsBackgroundRecording() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(false);

        TreeReference treeReference1 = new TreeReference();
        TreeReference treeReference2 = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference1, "low");
        recordAudioActionRegistry.listener.accept(treeReference2, "low");
        RobolectricHelpers.runLooper();

        viewModel.grantAudioPermission();
        verify(audioRecorder).start(new HashSet<TreeReference>() {
            {
                add(treeReference1);
                add(treeReference2);
            }
        }, Output.AAC_LOW);
    }

    @Test
    public void grantAudioPermission_whenActionsHaveDifferentQualities_usesFirstQuality() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(false);

        TreeReference treeReference1 = new TreeReference();
        TreeReference treeReference2 = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference1, "voice-only");
        recordAudioActionRegistry.listener.accept(treeReference2, "low");
        RobolectricHelpers.runLooper();

        viewModel.grantAudioPermission();
        verify(audioRecorder).start(new HashSet<TreeReference>() {
            {
                add(treeReference1);
                add(treeReference2);
            }
        }, Output.AMR);
    }

    @Test
    public void grantAudioPermission_setsPermissionRequiredToFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)).thenReturn(false);

        TreeReference treeReference1 = new TreeReference();
        recordAudioActionRegistry.listener.accept(treeReference1, "low");
        RobolectricHelpers.runLooper();

        viewModel.grantAudioPermission();
        assertThat(viewModel.isPermissionRequired().getValue(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void grantAudioPermission_whenThereWasNoPermissionCheck_throwsIllegalStateException() {
        viewModel.grantAudioPermission();
    }

    @Test
    public void setBackgroundRecordingEnabled_whenFalse_logsEventToAuditLog() {
        FormController formController = mock(FormController.class);
        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);
        viewModel.formLoaded(formController);

        when(clock.getCurrentTime()).thenReturn(1234L);
        viewModel.setBackgroundRecordingEnabled(false);
        verify(auditEventLogger).logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_DISABLED, true, 1234L);
    }

    @Test
    public void setBackgroundRecordingEnabled_whenTrue_logsEventToAuditLog() {
        FormController formController = mock(FormController.class);
        AuditEventLogger auditEventLogger = mock(AuditEventLogger.class);
        when(formController.getAuditEventLogger()).thenReturn(auditEventLogger);
        viewModel.formLoaded(formController);

        when(clock.getCurrentTime()).thenReturn(1234L);
        viewModel.setBackgroundRecordingEnabled(true);
        verify(auditEventLogger).logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_ENABLED, true, 1234L);
    }

    private static class FakeRecordAudioActionRegistry implements BackgroundAudioViewModel.RecordAudioActionRegistry {


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
