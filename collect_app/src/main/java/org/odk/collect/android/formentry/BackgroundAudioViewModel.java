package org.odk.collect.android.formentry;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.actions.recordaudio.RecordAudioActions;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_RECORDING;

public class BackgroundAudioViewModel extends ViewModel {

    private final AudioRecorder audioRecorder;
    private final PreferencesProvider preferencesProvider;
    private final RecordAudioActionRegistry recordAudioActionRegistry;
    private final PermissionsChecker permissionsChecker;

    private final MutableLiveData<Boolean> isPermissionRequired = new MutableLiveData<>(false);

    // These fields handle storing record action details while we're granting permissions
    private final HashSet<TreeReference> tempTreeReferences = new HashSet<>();
    private String tempQuality;

    public BackgroundAudioViewModel(AudioRecorder audioRecorder, PreferencesProvider preferencesProvider, RecordAudioActionRegistry recordAudioActionRegistry, PermissionsChecker permissionsChecker) {
        this.audioRecorder = audioRecorder;
        this.preferencesProvider = preferencesProvider;
        this.recordAudioActionRegistry = recordAudioActionRegistry;
        this.permissionsChecker = permissionsChecker;

        this.recordAudioActionRegistry.register((treeReference, quality) -> {
            new Handler(Looper.getMainLooper()).post(() -> handleRecordAction(treeReference, quality));
        });
    }

    @Override
    protected void onCleared() {
        recordAudioActionRegistry.unregister();
    }

    public LiveData<Boolean> isPermissionRequired() {
        return isPermissionRequired;
    }

    public boolean isBackgroundRecordingEnabled() {
        return preferencesProvider.getGeneralSharedPreferences().getBoolean(KEY_BACKGROUND_RECORDING, true);
    }

    public void setBackgroundRecordingEnabled(boolean enabled) {
        if (!enabled) {
            audioRecorder.cleanUp();
        }

        preferencesProvider.getGeneralSharedPreferences().edit().putBoolean(KEY_BACKGROUND_RECORDING, enabled).apply();
    }

    public boolean isBackgroundRecording() {
        return audioRecorder.isRecording() && audioRecorder.getCurrentSession().getValue().getId() instanceof Set;
    }

    public void grantAudioPermission() {
        isPermissionRequired.setValue(false);
        startBackgroundRecording(tempQuality, new HashSet<>(tempTreeReferences));
        
        tempTreeReferences.clear();
        tempQuality = null;
    }

    private void handleRecordAction(TreeReference treeReference, String quality) {
        if (isBackgroundRecordingEnabled()) {
            if (permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                if (isBackgroundRecording()) {
                    RecordingSession session = audioRecorder.getCurrentSession().getValue();
                    HashSet<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();
                    treeReferences.add(treeReference);
                } else {
                    HashSet<TreeReference> treeReferences = new HashSet<>();
                    treeReferences.add(treeReference);

                    startBackgroundRecording(quality, treeReferences);
                }
            } else {
                isPermissionRequired.setValue(true);

                tempTreeReferences.add(treeReference);
                if (tempQuality == null) {
                    tempQuality = quality;
                }
            }
        }
    }

    private void startBackgroundRecording(String quality, HashSet<TreeReference> treeReferences) {
        Output output = Output.AMR;
        if ("low".equals(quality)) {
            output = Output.AAC_LOW;
        } else if ("normal".equals(quality)) {
            output = Output.AAC;
        }

        audioRecorder.start(treeReferences, output);
    }

    public interface RecordAudioActionRegistry {

        void register(BiConsumer<TreeReference, String> listener);

        void unregister();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final AudioRecorder audioRecorder;
        private final PreferencesProvider preferencesProvider;
        private final PermissionsChecker permissionsChecker;

        @Inject
        public Factory(AudioRecorder audioRecorder, PreferencesProvider preferencesProvider, PermissionsChecker permissionsChecker) {
            this.audioRecorder = audioRecorder;
            this.preferencesProvider = preferencesProvider;
            this.permissionsChecker = permissionsChecker;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BackgroundAudioViewModel(audioRecorder, preferencesProvider, new BackgroundAudioViewModel.RecordAudioActionRegistry() {
                @Override
                public void register(BiConsumer<TreeReference, String> listener) {
                    RecordAudioActions.setRecordAudioListener(listener::accept);
                }

                @Override
                public void unregister() {
                    RecordAudioActions.setRecordAudioListener(null);
                }
            }, permissionsChecker);
        }
    }
}
