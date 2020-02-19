package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    SUCCESS,
    FORM_UPLOADER_IS_RUNNING,
    FORM_DOWNLOADER_IS_RUNNING,
    NOT_ENOUGH_SPACE,
    MOVING_FILES_FAILED;

    public static String getBannerText(StorageMigrationResult result, Context context) {
        if (result == null) {
            return context.getString(R.string.scoped_storage_banner_text);
        } else if (result == SUCCESS) {
            return context.getString(R.string.storage_migration_completed);
        } else {
            return context.getString(R.string.scoped_storage_banner_text)
                    + context.getString(R.string.last_attempt_failed)
                    + getResultMessage(result, context);
        }
    }

    private static String getResultMessage(StorageMigrationResult result, Context context) {
        switch (result) {
            case SUCCESS:
                return context.getString(R.string.storage_migration_completed);
            case NOT_ENOUGH_SPACE:
                return context.getString(R.string.storage_migration_not_enough_space);
            case FORM_UPLOADER_IS_RUNNING:
                return context.getString(R.string.storage_migration_form_uploader_is_running);
            case FORM_DOWNLOADER_IS_RUNNING:
                return context.getString(R.string.storage_migration_form_downloader_is_running);
            case MOVING_FILES_FAILED:
                return context.getString(R.string.storage_migration_failed);
            default:
                return null;
        }
    }
}