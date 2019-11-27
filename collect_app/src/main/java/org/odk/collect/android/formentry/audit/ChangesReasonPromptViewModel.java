package org.odk.collect.android.formentry.audit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class ChangesReasonPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);
    private boolean editingForm;
    private AuditEventLogger auditEventLogger;
    private String reason;

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public void setAuditEventLogger(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
        updateRequiresReasonToContinue();
    }

    public void savingForm() {
        updateRequiresReasonToContinue();
    }

    public void editingForm() {
        editingForm = true;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void saveReason(Long currentTime) {
        if (reason != null && !isBlank(reason)) {
            this.auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, currentTime, reason);
            requiresReasonToContinue.setValue(false);
        }
    }

    public String getReason() {
        return reason;
    }

    private void updateRequiresReasonToContinue() {
        requiresReasonToContinue.setValue(
                editingForm
                        && auditEventLogger != null
                        && auditEventLogger.isChangeReasonRequired()
                        && auditEventLogger.isChangesMade()
        );
    }
}
