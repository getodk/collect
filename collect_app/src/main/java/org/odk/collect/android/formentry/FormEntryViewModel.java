package org.odk.collect.android.formentry;

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
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.tasks.SaveToDiskTaskResult;
import org.odk.collect.utilities.Clock;

import java.lang.ref.WeakReference;

import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED;
import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED_AND_EXIT;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormEntryViewModel extends ViewModel {

    private final Clock clock;
    private final FormSaver formSaver;
    private final MutableLiveData<SaveResult> saveResult = new MutableLiveData<>(null);

    private String reason = "";

    @Nullable
    private AuditEventLogger auditEventLogger;

    @Nullable
    private AsyncTask saveTask;

    public FormEntryViewModel(Clock clock, FormSaver formSaver) {
        this.clock = clock;
        this.formSaver = formSaver;
    }

    public void setAuditEventLogger(@Nullable AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public void editingForm() {
        if (auditEventLogger != null) {
            auditEventLogger.setEditing(true);
        }
    }

    public LiveData<SaveResult> saveForm(Uri uri, boolean complete, String updatedSaveName, boolean viewExiting) {
        SaveRequest saveRequest = new SaveRequest(complete, viewExiting, updatedSaveName, uri);

        if (!requiresReasonToSave()) {
            saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest));
            saveToDisk(saveRequest);
        } else {
            saveResult.setValue(new SaveResult(SaveResult.State.CHANGE_REASON_REQUIRED, saveRequest));
        }

        return saveResult;
    }

    public boolean isSaving() {
        return saveResult.getValue() != null && saveResult.getValue().getState().equals(SaveResult.State.SAVING);
    }

    public boolean cancelSaving() {
        return saveTask.cancel(true);
    }

    public void setReason(@NonNull String reason) {
        this.reason = reason;
    }

    public boolean saveReason() {
        if (reason == null || isBlank(reason)) {
            return false;
        }

        if (auditEventLogger != null) {
            auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, clock.getCurrentTime(), reason);
        }

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

    private void setSaveRequestState(SaveToDiskTaskResult saveToDiskTaskResult, SaveResult.State state, SaveRequest request) {
        this.saveResult.setValue(new SaveResult(state, request, saveToDiskTaskResult.getSaveErrorMessage()));
    }

    private void saveToDisk(SaveRequest saveRequest) {
        saveTask = new SaveTask(saveRequest, formSaver, new SaveTask.Listener() {
            @Override
            public void onProgressPublished(String progress) {
                saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest, progress));
            }

            @Override
            public void onSaveComplete(SaveToDiskTaskResult saveToDiskTaskResult) {
                handleTaskResult(saveToDiskTaskResult);
            }
        }).execute();
    }

    private void handleTaskResult(SaveToDiskTaskResult taskResult) {
        SaveResult saveResult = this.saveResult.getValue();

        switch (taskResult.getSaveResult()) {
            case SAVED:
            case SAVED_AND_EXIT: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, clock.getCurrentTime());

                    if (saveResult.request.isViewExiting()) {
                        if (saveResult.request.isFormComplete()) {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, clock.getCurrentTime());
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, clock.getCurrentTime());
                        } else {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, clock.getCurrentTime());
                        }
                    }
                }

                setSaveRequestState(taskResult, SaveResult.State.SAVED, saveResult.request);
                break;
            }

            case SaveToDiskTask.SAVE_ERROR: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, clock.getCurrentTime());
                }

                setSaveRequestState(taskResult, SaveResult.State.SAVE_ERROR, saveResult.request);
                break;
            }

            case SaveToDiskTask.ENCRYPTION_ERROR: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, clock.getCurrentTime());
                }

                setSaveRequestState(taskResult, SaveResult.State.FINALIZE_ERROR, saveResult.request);
                break;
            }

            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY: {
                if (auditEventLogger != null) {
                    auditEventLogger.exitView();
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, clock.getCurrentTime());
                }

                setSaveRequestState(taskResult, SaveResult.State.CONSTRAINT_ERROR, saveResult.request);
                break;
            }
        }
    }

    private boolean requiresReasonToSave() {
        return auditEventLogger != null
                && auditEventLogger.isEditing()
                && auditEventLogger.isChangeReasonRequired()
                && auditEventLogger.isChangesMade();
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

    private static class SaveTask extends AsyncTask<Void, String, SaveToDiskTaskResult> {

        private final SaveRequest saveRequest;
        private final FormSaver formSaver;

        private final WeakReference<Listener> listenerRef;

        SaveTask(SaveRequest saveRequest, FormSaver formSaver, Listener listener) {
            this.saveRequest = saveRequest;
            this.formSaver = formSaver;
            this.listenerRef = new WeakReference<>(listener);
        }

        @Override
        protected SaveToDiskTaskResult doInBackground(Void... voids) {
            return formSaver.save(
                    saveRequest.uri,
                    saveRequest.formComplete,
                    saveRequest.updatedSaveName,
                    saveRequest.viewExiting,
                    this::publishProgress
            );
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Listener listener = listenerRef.get();
            if (listener != null) {
                listener.onProgressPublished(values[0]);
            }
        }

        @Override
        protected void onPostExecute(SaveToDiskTaskResult saveToDiskTaskResult) {
            Listener listener = listenerRef.get();
            if (listener != null) {
                listener.onSaveComplete(saveToDiskTaskResult);
            }
        }

        interface Listener {
            void onProgressPublished(String progress);

            void onSaveComplete(SaveToDiskTaskResult saveToDiskTaskResult);
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
