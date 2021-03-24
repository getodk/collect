package org.odk.collect.android.formmanagement.matchexactly;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.odk.collect.android.forms.FormSourceException;

public class SyncStatusRepository {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<FormSourceException> lastSyncFailure = new MutableLiveData<>(null);

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    public LiveData<FormSourceException> getSyncError() {
        return lastSyncFailure;
    }

    public void startSync() {
        syncing.postValue(true);
    }

    public void finishSync(@Nullable FormSourceException exception) {
        lastSyncFailure.postValue(exception);
        syncing.postValue(false);
    }
}
