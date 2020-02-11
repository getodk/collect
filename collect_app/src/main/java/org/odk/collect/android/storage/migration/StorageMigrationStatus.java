package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationStatus {
    PREPARING_SCOPED_STORAGE,
    CHECKING_APP_STATE,
    MOVING_FILES,
    MIGRATING_DATABASES;

    public static String getStatusMessage(StorageMigrationStatus status, Context context) {
        String message = null;
        switch (status) {
            case PREPARING_SCOPED_STORAGE:
                message = context.getString(R.string.storage_migration_status_1);
                break;
            case CHECKING_APP_STATE:
                message = context.getString(R.string.storage_migration_status_2);
                break;
            case MOVING_FILES:
                message = context.getString(R.string.storage_migration_status_3);
                break;
            case MIGRATING_DATABASES:
                message = context.getString(R.string.storage_migration_status_4);
                break;
        }
        return message;
    }
}
