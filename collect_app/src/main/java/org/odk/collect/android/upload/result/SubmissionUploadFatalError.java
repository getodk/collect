package org.odk.collect.android.upload.result;

import android.support.annotation.NonNull;

public class SubmissionUploadFatalError extends SubmissionUploadResult {
    public SubmissionUploadFatalError(Integer localizedMessageId, @NonNull String troubleshootingMessage) {
        super(false, true, localizedMessageId, troubleshootingMessage);
    }

    public SubmissionUploadFatalError(@NonNull String troubleshootingMessage) {
        this(null, troubleshootingMessage);
    }
}
