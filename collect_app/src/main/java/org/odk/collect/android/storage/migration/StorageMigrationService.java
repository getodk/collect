package org.odk.collect.android.storage.migration;

import android.content.Intent;

import androidx.lifecycle.LifecycleService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;

public class StorageMigrationService extends LifecycleService {
    public static final String STORAGE_MIGRATION_STATUS_INTENT = "storageMigrationStatusIntent";
    public static final String STORAGE_MIGRATION_STATUS = "storageMigrationStatus";

    public static final String STORAGE_MIGRATION_RESULT_INTENT = "storageMigrationResultIntent";
    public static final String STORAGE_MIGRATION_RESULT = "storageMigrationResult";

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        StorageStateProvider storageStateProvider = new StorageStateProvider();
        StorageEraser storageEraser = new StorageEraser(storagePathProvider);

        StorageMigrator storageMigrator = new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser);
        storageMigrator.getStatus().observe(this, this::sendStatus);

        new Thread() {
            @Override
            public void run() {
                StorageMigrator.isMigrationBeingPerformed = true;
                sendResult(storageMigrator.performStorageMigration());
                StorageMigrator.isMigrationBeingPerformed = false;
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendStatus(StorageMigrationStatus status) {
        Intent intent = new Intent(STORAGE_MIGRATION_STATUS_INTENT);
        intent.putExtra(STORAGE_MIGRATION_STATUS, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendResult(StorageMigrationResult result) {
        Intent intent = new Intent(STORAGE_MIGRATION_RESULT_INTENT);
        intent.putExtra(STORAGE_MIGRATION_RESULT, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
