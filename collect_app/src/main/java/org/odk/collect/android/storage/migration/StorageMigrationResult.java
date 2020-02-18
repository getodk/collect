package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    NO_RESULT(0),
    SUCCESS(1),
    FORM_UPLOADER_IS_RUNNING(2),
    FORM_DOWNLOADER_IS_RUNNING(3),
    NOT_ENOUGH_SPACE(4),
    MOVING_FILES_FAILED(5);

    private int resultCode;

    StorageMigrationResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public static StorageMigrationResult getResult(int resultCode) {
        switch (resultCode) {
            case 1:
                return SUCCESS;
            case 2:
                return FORM_UPLOADER_IS_RUNNING;
            case 3:
                return FORM_DOWNLOADER_IS_RUNNING;
            case 4:
                return NOT_ENOUGH_SPACE;
            case 5:
                return MOVING_FILES_FAILED;
            default:
                return NO_RESULT;
        }
    }

    public String getBannerText(StorageMigrationResult result, Context context) {
        switch (result) {
            case NO_RESULT:
                return context.getString(R.string.scoped_storage_banner_text);
            case SUCCESS:
                return context.getString(R.string.storage_migration_completed);
            default:
                return context.getString(R.string.scoped_storage_banner_text)
                        + context.getString(R.string.last_attempt_failed)
                        + getResultMessage(result, context);
        }
    }

    private String getResultMessage(StorageMigrationResult result, Context context) {
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