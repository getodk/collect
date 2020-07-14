package org.odk.collect.android.formmanagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SyncStatusRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private boolean started;

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public synchronized boolean startSync() {
        if (started) {
            return false;
        } else {
            syncing.postValue(true);
            started = true;
            return true;
        }
    }

    public void finishSync() {
        syncing.postValue(false);
        started = false;
    }
}
