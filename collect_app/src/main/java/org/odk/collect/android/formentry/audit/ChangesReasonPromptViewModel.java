package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class ChangesReasonPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);
    private final MutableLiveData<SaveRequest> saveRequest = new MutableLiveData<>(null);

    private AuditEventLogger auditEventLogger;
    private String reason;
    private SaveRequest lastSaveRequest;

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public LiveData<SaveRequest> saveRequest() {
        return saveRequest;
    }

    public void setAuditEventLogger(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public void saveForm(boolean complete, String updatedSaveName, boolean exitAfter) {
        lastSaveRequest = new SaveRequest(complete, updatedSaveName, exitAfter);

        if (!requiresReasonToSave()) {
            saveRequest.setValue(lastSaveRequest);
        } else {
            requiresReasonToContinue.setValue(true);
        }
    }

    public void saveComplete() {
        saveRequest.setValue(null);
    }

    public void editingForm() {
        auditEventLogger.setEditing(true);
    }

    public void promptDismissed() {
        requiresReasonToContinue.setValue(false);
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void saveReason(Long currentTime) {
        if (reason != null && !isBlank(reason)) {
            this.auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, currentTime, reason);
            requiresReasonToContinue.setValue(false);
            saveRequest.setValue(lastSaveRequest);
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

    public static class SaveRequest {

        private final boolean complete;
        private final String updatedSaveName;
        private final boolean exitAfter;

        public SaveRequest(boolean complete, String updatedSaveName, boolean exitAfter) {

            this.complete = complete;
            this.updatedSaveName = updatedSaveName;
            this.exitAfter = exitAfter;
        }

        public boolean isComplete() {
            return complete;
        }

        public String getUpdatedSaveName() {
            return updatedSaveName;
        }

        public boolean isExitAfter() {
            return exitAfter;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ChangesReasonPromptViewModel();
        }
    }
}
