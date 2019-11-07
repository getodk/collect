package org.odk.collect.android.formentry.audit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IdentityPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> formEntryCancelled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> identitySet = new MutableLiveData<>(false);

    private String identity = "";

    public void setIdentity(String identity) {
        this.identity = identity;
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
}
