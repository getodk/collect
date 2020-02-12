package org.odk.collect.android.storage.migration;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.application.Collect;

import javax.inject.Inject;

public class StorageMigrationService extends Service {
    @Inject
    StorageMigrator storageMigrator;

    @Override
    public void onCreate() {
        super.onCreate();
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                StorageMigrator.isMigrationBeingPerformed = true;
                storageMigrator.performStorageMigration();
                StorageMigrator.isMigrationBeingPerformed = false;
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
