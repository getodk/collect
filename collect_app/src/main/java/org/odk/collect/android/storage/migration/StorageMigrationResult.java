package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    SUCCESS,
    FORM_UPLOADER_IS_RUNNING,
    FORM_DOWNLOADER_IS_RUNNING,
    NOT_ENOUGH_SPACE,
    MOVING_FILES_FAILED;

    public String getErrorResultMessage(Context context) {
        String errorMessage = context.getString(R.string.error) + " ";
        switch (this) {
            case NOT_ENOUGH_SPACE:
                return errorMessage + context.getString(R.string.storage_migration_not_enough_space);
            case FORM_UPLOADER_IS_RUNNING:
                return errorMessage + context.getString(R.string.storage_migration_form_uploader_is_running);
            case FORM_DOWNLOADER_IS_RUNNING:
                return errorMessage + context.getString(R.string.storage_migration_form_downloader_is_running);
            case MOVING_FILES_FAILED:
                return errorMessage + context.getString(R.string.storage_migration_failed);
            default:
                return errorMessage;
        }
    }
}