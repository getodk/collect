package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormEntryViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);

    @Nullable
    private MutableLiveData<SaveRequest> saveRequest;

    @Nullable
    private AuditEventLogger auditEventLogger;

    @Nullable
    private String reason;

    @Nullable
    private SaveRequest lastSaveRequest;

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public void setAuditEventLogger(@Nullable AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public LiveData<SaveRequest> saveForm(boolean complete, String updatedSaveName, boolean exitAfter) {
        saveRequest = new MutableLiveData<>(null);
        lastSaveRequest = new SaveRequest(complete, updatedSaveName, exitAfter);

        if (!requiresReasonToSave()) {
            saveRequest.setValue(lastSaveRequest);
        } else {
            requiresReasonToContinue.setValue(true);
        }

        return saveRequest;
    }

    public void saveComplete() {
        saveRequest = null;
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

            if (saveRequest != null) {
                saveRequest.setValue(lastSaveRequest);
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
            return (T) new FormEntryViewModel();
        }
    }
}
