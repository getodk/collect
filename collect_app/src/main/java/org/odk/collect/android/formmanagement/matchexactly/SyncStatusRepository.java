package org.odk.collect.android.formmanagement.matchexactly;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SyncStatusRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> lastSyncFailure = new MutableLiveData<>(false);

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public void startSync() {
        syncing.postValue(true);
    }

    public void finishSync(boolean success) {
        lastSyncFailure.postValue(!success);
        syncing.postValue(false);
    }

    public LiveData<Boolean> isOutOfSync() {
        return lastSyncFailure;
    }
}
