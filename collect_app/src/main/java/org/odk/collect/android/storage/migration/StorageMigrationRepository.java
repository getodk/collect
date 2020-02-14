package org.odk.collect.android.storage.migration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class StorageMigrationRepository {
    private final MutableLiveData<StorageMigrationResult> migrationResult = new MutableLiveData<>();

    public LiveData<StorageMigrationResult> getResult() {
        return migrationResult;
    }

    public void setResult(StorageMigrationResult storageMigrationResult) {
        migrationResult.postValue(storageMigrationResult);
    }
}
