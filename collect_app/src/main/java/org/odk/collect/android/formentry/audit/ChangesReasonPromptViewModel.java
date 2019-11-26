package org.odk.collect.android.formentry.audit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChangesReasonPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);
    private boolean editingForm;
    private AuditEventLogger auditEventLogger;

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
