package org.odk.collect.android.storage.migration;

import android.os.AsyncTask;

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
        return new StorageMigrator().performStorageMigration();
    }

    @Override
    protected void onPostExecute(StorageMigrationResult result) {
        super.onPostExecute(result);
        StorageMigrator.isMigrationBeingPerformed = false;
        listener.onComplete(result);
    }
}