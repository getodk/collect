package org.odk.collect.android.storage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StorageMigrationViewModel extends ViewModel {

    public LiveData<StorageMigrationResult> performMigration() {
        MutableLiveData<StorageMigrationResult> result = new MutableLiveData<>();
        new StorageMigratorTask(result::setValue).execute();

        return result;
    }
}
