package org.odk.collect.android.storage.migration;

import android.os.AsyncTask;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;

public class StorageMigratorTask extends AsyncTask<Void, Void, StorageMigrationResult> {

    interface Listener {
        void onComplete(StorageMigrationResult result);
    }

    private final Listener listener;

    StorageMigratorTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected StorageMigrationResult doInBackground(Void... voids) {
        StorageMigrator.isMigrationBeingPerformed = true;

        StoragePathProvider storagePathProvider = new StoragePathProvider();
        StorageStateProvider storageStateProvider = new StorageStateProvider();
        StorageEraser storageEraser = new StorageEraser(storagePathProvider);

        return new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser).performStorageMigration();
    }

    @Override
    protected void onPostExecute(StorageMigrationResult result) {
        super.onPostExecute(result);
        StorageMigrator.isMigrationBeingPerformed = false;
        listener.onComplete(result);
    }
}