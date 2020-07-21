package org.odk.collect.android.formmanagement.matchexactly;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SyncStatusRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> lastSyncFailure = new MutableLiveData<>(false);
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

    public void finishSync(boolean success) {
        lastSyncFailure.postValue(!success);
        syncing.postValue(false);
        started = false;
    }

    public LiveData<Boolean> isOutOfSync() {
        return lastSyncFailure;
    }
}
