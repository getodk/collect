package org.odk.collect.android.formmanagement.matchexactly;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.odk.collect.android.openrosa.api.FormApiException;

public class SyncStatusRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<FormApiException> lastSyncFailure = new MutableLiveData<>(null);

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public void startSync() {
        syncing.postValue(true);
    }

    public void finishSync(@Nullable FormApiException exception) {
        lastSyncFailure.postValue(exception);
        syncing.postValue(false);
    }

    public LiveData<FormApiException> getSyncError() {
        return lastSyncFailure;
    }
}
