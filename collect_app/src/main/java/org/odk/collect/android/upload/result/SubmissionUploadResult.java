package org.odk.collect.android.upload.result;

import android.support.annotation.NonNull;

import org.odk.collect.android.application.Collect;

public abstract class SubmissionUploadResult {
    private final boolean success;
    private final boolean fatalError;

    private final Integer localizedMessageId;

    @NonNull
    private final String troubleshootingMessage;

    SubmissionUploadResult(boolean success, boolean fatalError, Integer localizedMessageId,
                           @NonNull String troubleshootingMessage) {
        this.success = success;
        this.fatalError = fatalError;
        this.localizedMessageId = localizedMessageId;
        this.troubleshootingMessage = troubleshootingMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFatalError() {
        return fatalError;
    }

    /**
     * Returns a message to display to the user. A localized message takes precedence. If one
     * is not available, use the troubleshooting message.
     */
    @NonNull
    public String getDisplayMessage() {
        if (localizedMessageId != null) {
            return Collect.getInstance().getString(localizedMessageId);
        } else {
            return troubleshootingMessage;
        }
    }
}
