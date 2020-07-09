package org.odk.collect.android.formmanagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ServerFormsSyncRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public void startSync() {
        syncing.setValue(true);
    }

    public void finishSync() {
        syncing.setValue(false);
    }
}
