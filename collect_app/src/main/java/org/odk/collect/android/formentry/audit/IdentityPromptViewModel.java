package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class IdentityPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> formEntryCancelled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> identitySet = new MutableLiveData<>(false);
    private final AuditEventLogger auditEventLogger;

    private String identity = "";

    public IdentityPromptViewModel(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public void setIdentity(String identity) {
        this.identity = identity;

        auditEventLogger.setUser(identity);
        identitySet.setValue(!this.identity.isEmpty());
    }

    public void promptClosing() {
        if (identity.isEmpty()) {
            formEntryCancelled.setValue(true);
        }
    }

    public LiveData<Boolean> isIdentitySet() {
        return identitySet;
    }

    public LiveData<Boolean> isFormEntryCancelled() {
        return formEntryCancelled;
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
