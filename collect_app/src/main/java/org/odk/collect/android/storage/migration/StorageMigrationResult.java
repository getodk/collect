package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    SUCCESS,
    FORM_UPLOADER_IS_RUNNING,
    FORM_DOWNLOADER_IS_RUNNING,
    NOT_ENOUGH_SPACE,
    MOVING_FILES_SUCCEEDED,
    MOVING_FILES_FAILED,
    MIGRATING_DATABASE_PATHS_SUCCEEDED,
    MIGRATING_DATABASE_PATHS_FAILED;

    public static String getResultMessage(StorageMigrationResult result, Context context) {
        String message = null;
        switch (result) {
            case SUCCESS:
                message = context.getString(R.string.storage_migration_completed);
                break;
            case NOT_ENOUGH_SPACE:
                message = context.getString(R.string.storage_migration_not_enough_space);
                break;
            case FORM_UPLOADER_IS_RUNNING:
                message = context.getString(R.string.storage_migration_form_uploader_is_running);
                break;
            case FORM_DOWNLOADER_IS_RUNNING:
                message = context.getString(R.string.storage_migration_form_downloader_is_running);
                break;
            case MOVING_FILES_FAILED:
            case MIGRATING_DATABASE_PATHS_FAILED:
                message = context.getString(R.string.storage_migration_failed);
                break;
        }
        return message;
    }
}