package org.odk.collect.android.formmanagement.matchexactly;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.forms.FormSourceException;

import javax.inject.Singleton;

@Singleton
public class SyncStatusAppState {

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<FormSourceException> lastSyncFailure = new MutableLiveData<>(null);
    private final Context context;

    public SyncStatusAppState(Context context) {
        this.context = context;
    }

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
        context.getContentResolver().notifyChange(FormsProviderAPI.CONTENT_URI, null);
    }
}
