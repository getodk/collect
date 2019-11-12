package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class IdentityPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> formEntryCancelled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> requiresIdentity = new MutableLiveData<>(false);
    private final AuditEventLogger auditEventLogger;

    public IdentityPromptViewModel(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
        updateRequiresIdentity();
    }

    public LiveData<Boolean> requiresIdentity() {
        return requiresIdentity;
    }

    public LiveData<Boolean> isFormEntryCancelled() {
        return formEntryCancelled;
    }

    public void setIdentity(String identity) {

        auditEventLogger.setUser(identity);
        updateRequiresIdentity();
    }

    public void promptClosing() {
        if (requiresIdentity.getValue()) {
            formEntryCancelled.setValue(true);
        }
    }

    private void updateRequiresIdentity() {
        this.requiresIdentity.setValue(
                auditEventLogger.isUserRequired() &&
                        (auditEventLogger.getUser() == null || auditEventLogger.getUser().isEmpty()));
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final AuditEventLogger auditEventLogger;

        public Factory(AuditEventLogger auditEventLogger) {
            this.auditEventLogger = auditEventLogger;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new IdentityPromptViewModel(auditEventLogger);
        }
    }
}
