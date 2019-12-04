package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class ChangesReasonPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> readyToSave = new MutableLiveData<>(false);

    private AuditEventLogger auditEventLogger;
    private String reason;

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public LiveData<Boolean> readyToSave() {
        return readyToSave;
    }

    public void setAuditEventLogger(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public void savingForm() {
        if (!requiresReasonToSave()) {
            readyToSave.setValue(true);
        } else {
            requiresReasonToContinue.setValue(true);
        }
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
            readyToSave.setValue(true);
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

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ChangesReasonPromptViewModel();
        }
    }
}
