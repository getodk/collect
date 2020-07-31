package org.odk.collect.android.storage.migration;

import android.content.Context;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    SUCCESS,
    NOT_ENOUGH_SPACE,
    MOVING_FILES_FAILED,
    CHANGES_IN_PROGRESS;

    public String getErrorResultMessage(Context context) {
        String errorMessage = context.getString(R.string.error) + " ";
        switch (this) {
            case NOT_ENOUGH_SPACE:
                return errorMessage + context.getString(R.string.storage_migration_not_enough_space);
            case CHANGES_IN_PROGRESS:
                return errorMessage + context.getString(R.string.changes_in_progress);
            case MOVING_FILES_FAILED:
                return errorMessage + context.getString(R.string.storage_migration_failed);
            default:
                return errorMessage;
        }
    }
}