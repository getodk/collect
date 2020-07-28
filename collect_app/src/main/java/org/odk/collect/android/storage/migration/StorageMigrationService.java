package org.odk.collect.android.storage.migration;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.backgroundwork.ChangeLock;

import javax.inject.Inject;
import javax.inject.Named;

public class StorageMigrationService extends IntentService {

    public static final String SERVICE_NAME = "StorageMigrationService";

    @Inject
    StorageMigrator storageMigrator;

    @Inject
    @Named("FORMS")
    ChangeLock formsLock;

    @Inject
    @Named("INSTANCES")
    ChangeLock instancesLock;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    public StorageMigrationService() {
        super(SERVICE_NAME);
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        instancesLock.withLock(acquiredInstancesLock -> {
            formsLock.withLock(acquiredFormsLock -> {
                if (acquiredInstancesLock && acquiredFormsLock) {
                    storageMigrator.performStorageMigration();
                } else {
                    storageMigrationRepository.setResult(StorageMigrationResult.CHANGES_IN_PROGRESS);
                    storageMigrationRepository.markMigrationEnd();
                }

                return null;
            });

            return null;
        });
    }
}
