package org.odk.collect.android.formentry;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.Clock;

import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED;
import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED_AND_EXIT;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormEntryViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);
    private final Clock clock;
    private final FormSaver formSaver;

    @Nullable
    private MutableLiveData<SaveResult> saveResult;

    @Nullable
    private AuditEventLogger auditEventLogger;

    @Nullable
    private String reason;

    @Nullable
    private SaveRequest lastSaveRequest;

    private AsyncTask<Void, Void, org.odk.collect.android.tasks.SaveResult> task;

    public FormEntryViewModel(Clock clock, FormSaver formSaver) {
        this.clock = clock;
        this.formSaver = formSaver;
    }

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public void setAuditEventLogger(@Nullable AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public LiveData<SaveResult> saveForm(Uri uri, boolean complete, String updatedSaveName, boolean viewExiting) {
        saveResult = new MutableLiveData<>(null);
        lastSaveRequest = new SaveRequest(complete, viewExiting, updatedSaveName, uri);

        if (!requiresReasonToSave()) {
            saveToDisk(uri, complete, updatedSaveName, viewExiting);
        } else {
            requiresReasonToContinue.setValue(true);
            saveResult.setValue(new SaveResult(SaveResult.State.CHANGE_REASON_REQUIRED));
        }

        return saveResult;
    }

    public void editingForm() {
        if (auditEventLogger != null) {
            auditEventLogger.setEditing(true);
        }
    }

    public void promptDismissed() {
        requiresReasonToContinue.setValue(false);
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void saveReason(Long currentTime) {
        if (reason != null && !isBlank(reason)) {
            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, currentTime, reason);
            }

            requiresReasonToContinue.setValue(false);

            if (saveResult != null) {
                saveToDisk(lastSaveRequest.uri, lastSaveRequest.formComplete, lastSaveRequest.updatedSaveName, lastSaveRequest.viewExiting);
            }
        }
    }

    public String getReason() {
        return reason;
    }

    private boolean requiresReasonToSave() {
        return auditEventLogger != null
                && auditEventLogger.isEditing()
                && auditEventLogger.isChangeReasonRequired()
                && auditEventLogger.isChangesMade();
    }

    public boolean isSaving() {
        return saveResult != null && saveResult.getValue().getState().equals(SaveResult.State.SAVING);
    }

    public boolean cancelSaving() {
        return task.cancel(true);
    }

    private void setSaveRequestState(org.odk.collect.android.tasks.SaveResult saveResult, SaveResult.State finalizeError) {
        this.saveResult.setValue(new SaveResult(finalizeError, saveResult.getSaveErrorMessage()));
    }

    @SuppressLint("StaticFieldLeak")
    private void saveToDisk(Uri uri, boolean complete, String updatedSaveName, boolean viewExiting) {
        saveResult.setValue(new SaveResult(SaveResult.State.SAVING));

        task = new AsyncTask<Void, Void, org.odk.collect.android.tasks.SaveResult>() {

            @Override
            protected org.odk.collect.android.tasks.SaveResult doInBackground(Void... voids) {
                return formSaver.save(uri, complete, updatedSaveName, viewExiting, progress -> {
                    saveResult.postValue(new SaveResult(SaveResult.State.SAVING, progress));
                });
            }

            @Override
            protected void onPostExecute(org.odk.collect.android.tasks.SaveResult saveResult) {
                saveToDiskTaskComplete(saveResult, clock.getCurrentTime());
            }
        }.execute();
    }

    private void saveToDiskTaskComplete(org.odk.collect.android.tasks.SaveResult saveResult, long currentTime) {
        switch (saveResult.getSaveResult()) {
            case SAVED:
            case SAVED_AND_EXIT: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, currentTime);

                    if (lastSaveRequest.isViewExiting()) {
                        if (lastSaveRequest.isFormComplete()) {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, currentTime);
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, currentTime);
                        } else {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, currentTime);
                        }
                    }
                }

                setSaveRequestState(saveResult, SaveResult.State.SAVED);
                break;
            }

            case SaveToDiskTask.SAVE_ERROR: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, currentTime);
                }

                setSaveRequestState(saveResult, SaveResult.State.SAVE_ERROR);
                break;
            }

            case SaveToDiskTask.ENCRYPTION_ERROR: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, currentTime);
                }

                setSaveRequestState(saveResult, SaveResult.State.FINALIZE_ERROR);
                break;
            }

            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY: {
                if (auditEventLogger != null) {
                    auditEventLogger.exitView();
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, currentTime);
                }

                setSaveRequestState(saveResult, SaveResult.State.CONSTRAINT_ERROR);
                break;
            }
        }
    }

    public static class SaveResult {

        private final State state;
        private final String message;

        SaveResult(State state) {
            this(state, null);
        }

        SaveResult(State state, String message) {
            this.state = state;
            this.message = message;
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
    }

    private static class SaveRequest {

        private final boolean formComplete;
        private final boolean viewExiting;
        private final String updatedSaveName;
        private final Uri uri;

        SaveRequest(boolean formComplete, boolean viewExiting, String updatedSaveName, Uri uri) {
            this.formComplete = formComplete;
            this.viewExiting = viewExiting;
            this.updatedSaveName = updatedSaveName;
            this.uri = uri;
        }

        boolean isFormComplete() {
            return formComplete;
        }

        boolean isViewExiting() {
            return viewExiting;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(System::currentTimeMillis, new DiskFormSaver());
        }
    }
}
