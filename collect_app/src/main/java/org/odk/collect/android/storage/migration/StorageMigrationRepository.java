package org.odk.collect.android.storage.migration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class StorageMigrationRepository {
    private MutableLiveData<StorageMigrationResult> migrationResult = new MutableLiveData<>();

    private boolean isMigrationBeingPerformed;

    public LiveData<StorageMigrationResult> getResult() {
        return migrationResult;
    }

    public void setResult(StorageMigrationResult storageMigrationResult) {
        migrationResult.postValue(storageMigrationResult);
    }

    public boolean isMigrationBeingPerformed() {
        return isMigrationBeingPerformed;
    }

    void markMigrationStart() {
        isMigrationBeingPerformed = true;
    }

    void markMigrationEnd() {
        isMigrationBeingPerformed = false;
    }

    public void clearResult() {
        migrationResult = new MutableLiveData<>();
    }
}
