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

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void save(Long currentTime) {
        if (reason != null && !isBlank(reason)) {
            this.auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, currentTime, reason);
            requiresReasonToContinue.setValue(false);
        }
    }

    private void updateRequiresReasonToContinue() {
        requiresReasonToContinue.setValue(
                auditEventLogger != null
                        && auditEventLogger.isChangeReasonRequired()
                        && editingForm
        );
    }

    public void editingForm() {
        editingForm = true;
    }
}
