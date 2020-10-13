package org.odk.collect.android.formentry.saving;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryController;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.helpers.InstancesDaoHelper;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.formentry.RequiresFormController;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditUtils;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.utilities.Clock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.tasks.SaveFormToDisk.SAVED;
import static org.odk.collect.android.tasks.SaveFormToDisk.SAVED_AND_EXIT;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormSaveViewModel extends ViewModel implements ProgressDialogFragment.Cancellable, RequiresFormController, QuestionMediaManager {
    public static final String ORIGINAL_FILES = "originalFiles";
    public static final String RECENT_FILES = "recentFiles";

    private final SavedStateHandle stateHandle;
    private final Clock clock;
    private final FormSaver formSaver;
    private final MediaUtils mediaUtils;

    private final MutableLiveData<SaveResult> saveResult = new MutableLiveData<>(null);
    private String reason = "";

    private Map<String, String> originalFiles = new HashMap<>();
    private Map<String, String> recentFiles = new HashMap<>();

    @Nullable
    private FormController formController;

    @Nullable
    private AsyncTask<Void, String, SaveToDiskResult> saveTask;

    private final Analytics analytics;

    public FormSaveViewModel(SavedStateHandle stateHandle, Clock clock, FormSaver formSaver, MediaUtils mediaUtils, Analytics analytics) {
        this.stateHandle = stateHandle;
        this.clock = clock;
        this.formSaver = formSaver;
        this.mediaUtils = mediaUtils;
        this.analytics = analytics;

        if (stateHandle.get(ORIGINAL_FILES) != null) {
            originalFiles = stateHandle.get(ORIGINAL_FILES);
        }
        if (stateHandle.get(RECENT_FILES) != null) {
            recentFiles = stateHandle.get(RECENT_FILES);
        }
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;
    }

    public void editingForm() {
        if (formController == null) {
            return;
        }

        formController.getAuditEventLogger().setEditing(true);
    }

    public void saveAnswersForScreen(HashMap<FormIndex, IAnswerData> answers) {
        if (formController == null) {
            return;
        }

        try {
            formController.saveAllScreenAnswers(answers, false);
        } catch (JavaRosaException ignored) {
            // ignored
        }

        formController.getAuditEventLogger().flush();
    }

    public void saveForm(Uri instanceContentURI, boolean shouldFinalize, String updatedSaveName, boolean viewExiting) {
        if (isSaving() || formController == null) {
            return;
        }

        formController.getAuditEventLogger().flush();

        SaveRequest saveRequest = new SaveRequest(instanceContentURI, viewExiting, updatedSaveName, shouldFinalize);

        if (!requiresReasonToSave()) {
            this.saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest));
            saveToDisk(saveRequest);
        } else {
            this.saveResult.setValue(new SaveResult(SaveResult.State.CHANGE_REASON_REQUIRED, saveRequest));
        }
    }

    // Cleanup when user exits a form without saving
    public void ignoreChanges() {
        ExternalDataManager manager = Collect.getInstance().getExternalDataManager();
        if (manager != null) {
            manager.close();
        }

        if (formController != null) {
            formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, System.currentTimeMillis());

            if (formController.getInstanceFile() != null) {
                SaveFormToDisk.removeSavepointFiles(formController.getInstanceFile().getName());

                // if it's not already saved, erase everything
                if (!InstancesDaoHelper.isInstanceAvailable(getAbsoluteInstancePath())) {
                    // delete media first
                    String instanceFolder = formController.getInstanceFile().getParent();
                    Timber.i("Attempting to delete: %s", instanceFolder);
                    File file = formController.getInstanceFile().getParentFile();
                    int images = MediaUtils.deleteImagesInFolderFromMediaProvider(file);
                    int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(file);
                    int video = MediaUtils.deleteVideoInFolderFromMediaProvider(file);

                    Timber.i("Removed from content providers: %d image files, %d audio files and %d audio files.",
                            images, audio, video);
                    FileUtils.purgeMediaPath(instanceFolder);
                }
            }
        }

        clearMediaFiles();
    }

    @Nullable
    public String getAbsoluteInstancePath() {
        return formController != null ? formController.getAbsoluteInstancePath() : null;
    }

    public boolean isSaving() {
        return saveResult.getValue() != null && saveResult.getValue().getState().equals(SaveResult.State.SAVING);
    }

    @Override
    public boolean cancel() {
        if (saveTask != null) {
            return saveTask.cancel(true);
        } else {
            return false;
        }
    }

    public void setReason(@NonNull String reason) {
        this.reason = reason;
    }

    public boolean saveReason() {
        if (reason == null || isBlank(reason) || formController == null) {
            return false;
        }

        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, clock.getCurrentTime(), reason);

        if (saveResult.getValue() != null) {
            SaveRequest request = saveResult.getValue().request;
            saveResult.setValue(new SaveResult(SaveResult.State.SAVING, request));
            saveToDisk(request);
        }

        return true;
    }

    public String getReason() {
        return reason;
    }

    private void saveToDisk(SaveRequest saveRequest) {

        saveTask = new SaveTask(saveRequest, formSaver, formController, mediaUtils, new SaveTask.Listener() {
            @Override
            public void onProgressPublished(String progress) {
                saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest, progress));
            }

            @Override
            public void onComplete(SaveToDiskResult saveToDiskResult) {
                handleTaskResult(saveToDiskResult, saveRequest);
                clearMediaFiles();
            }
        }, analytics, new ArrayList<>(originalFiles.values())).execute();
    }

    private void handleTaskResult(SaveToDiskResult taskResult, SaveRequest saveRequest) {
        if (formController == null) {
            return;
        }

        switch (taskResult.getSaveResult()) {
            case SAVED:
            case SAVED_AND_EXIT: {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, clock.getCurrentTime());

                if (saveRequest.viewExiting) {
                    if (saveRequest.shouldFinalize) {
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, clock.getCurrentTime());
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, clock.getCurrentTime());
                    } else {
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, clock.getCurrentTime());
                    }
                } else {
                    AuditUtils.logCurrentScreen(formController, formController.getAuditEventLogger(), clock.getCurrentTime());
                }

                saveResult.setValue(new SaveResult(SaveResult.State.SAVED, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case SaveFormToDisk.SAVE_ERROR: {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, clock.getCurrentTime());
                saveResult.setValue(new SaveResult(SaveResult.State.SAVE_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case SaveFormToDisk.ENCRYPTION_ERROR: {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, clock.getCurrentTime());
                saveResult.setValue(new SaveResult(SaveResult.State.FINALIZE_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY: {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, clock.getCurrentTime());
                saveResult.setValue(new SaveResult(SaveResult.State.CONSTRAINT_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }
        }
    }

    public LiveData<SaveResult> getSaveResult() {
        return saveResult;
    }

    public void resumeFormEntry() {
        saveResult.setValue(null);
    }

    private boolean requiresReasonToSave() {
        return formController != null
                && formController.getAuditEventLogger().isEditing()
                && formController.getAuditEventLogger().isChangeReasonRequired()
                && formController.getAuditEventLogger().isChangesMade();
    }

    public String getFormName() {
        if (formController == null) {
            return null;
        }
        return formController.getFormTitle();
    }

    @Override
    public void deleteAnswerFile(String questionIndex, String fileName) {
        if (questionIndex != null && fileName != null) {
            // We don't want to delete the "original" answer file as we might need to restore it
            // but we can delete any follow up deletions
            if (originalFiles.containsKey(questionIndex)) {
                mediaUtils.deleteImageFileFromMediaProvider(fileName);
            } else {
                originalFiles.put(questionIndex, fileName);
                stateHandle.set(ORIGINAL_FILES, originalFiles);
            }
        }
    }

    @Override
    public void replaceAnswerFile(String questionIndex, String fileName) {
        if (questionIndex != null && fileName != null) {
            // If we're replacing an answer's file for a second time we can just get rid of the
            // first (replacement) file we were going to use
            if (recentFiles.containsKey(questionIndex)) {
                mediaUtils.deleteImageFileFromMediaProvider(recentFiles.get(questionIndex));
            }
            recentFiles.put(questionIndex, fileName);
            stateHandle.set(RECENT_FILES, recentFiles);
        }
    }

    @Override
    @Nullable
    public File getAnswerFile(String fileName) {
        if (formController != null && formController.getInstanceFile() != null) {
            return new File(formController.getInstanceFile().getParent(), fileName);
        } else {
            return null;
        }
    }

    private void clearMediaFiles() {
        originalFiles.clear();
        recentFiles.clear();
    }

    public static class SaveResult {
        private final State state;
        private final String message;
        private final SaveRequest request;

        SaveResult(State state, SaveRequest request) {
            this(state, request, null);
        }

        SaveResult(State state, SaveRequest request, String message) {
            this.state = state;
            this.message = message;
            this.request = request;
        }

        public State getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public enum State {
            CHANGE_REASON_REQUIRED,
            SAVING,
            SAVED,
            SAVE_ERROR,
            FINALIZE_ERROR,
            CONSTRAINT_ERROR
        }

        public SaveRequest getRequest() {
            return request;
        }
    }

    public static class SaveRequest {

        private final boolean shouldFinalize;
        private final boolean viewExiting;
        private final String updatedSaveName;
        private final Uri uri;

        SaveRequest(Uri instanceContentURI, boolean viewExiting, String updatedSaveName, boolean shouldFinalize) {
            this.shouldFinalize = shouldFinalize;
            this.viewExiting = viewExiting;
            this.updatedSaveName = updatedSaveName;
            this.uri = instanceContentURI;
        }

        public boolean shouldFinalize() {
            return shouldFinalize;
        }

        public boolean viewExiting() {
            return viewExiting;
        }
    }

    private static class SaveTask extends AsyncTask<Void, String, SaveToDiskResult> {

        private final SaveRequest saveRequest;
        private final FormSaver formSaver;

        private final Listener listener;
        private final FormController formController;
        private final MediaUtils mediaUtils;
        private final Analytics analytics;
        private final ArrayList<String> tempFiles;

        SaveTask(SaveRequest saveRequest, FormSaver formSaver, FormController formController, MediaUtils mediaUtils,
                 Listener listener, Analytics analytics, ArrayList<String> tempFiles) {
            this.saveRequest = saveRequest;
            this.formSaver = formSaver;
            this.listener = listener;
            this.formController = formController;
            this.mediaUtils = mediaUtils;
            this.analytics = analytics;
            this.tempFiles = tempFiles;
        }

        @Override
        protected SaveToDiskResult doInBackground(Void... voids) {
            return formSaver.save(saveRequest.uri, formController,
                    mediaUtils, saveRequest.shouldFinalize,
                    saveRequest.viewExiting, saveRequest.updatedSaveName,
                    this::publishProgress, analytics, tempFiles
            );
        }

        @Override
        protected void onProgressUpdate(String... values) {
            listener.onProgressPublished(values[0]);
        }

        @Override
        protected void onPostExecute(SaveToDiskResult saveToDiskResult) {
            listener.onComplete(saveToDiskResult);
        }

        interface Listener {
            void onProgressPublished(String progress);

            void onComplete(SaveToDiskResult saveToDiskResult);
        }
    }

    public static class Factory extends AbstractSavedStateViewModelFactory {
        private final Analytics analytics;

        public Factory(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs, Analytics analytics) {
            super(owner, defaultArgs);
            this.analytics = analytics;
        }

        @NonNull
        @Override
        protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
            return (T) new FormSaveViewModel(handle, System::currentTimeMillis, new DiskFormSaver(), new MediaUtils(), analytics);
        }
    }
}
