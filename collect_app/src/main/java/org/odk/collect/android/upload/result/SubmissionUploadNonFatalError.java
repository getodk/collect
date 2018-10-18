package org.odk.collect.android.upload.result;

import android.support.annotation.NonNull;

public class SubmissionUploadNonFatalError extends SubmissionUploadResult {
        public SubmissionUploadNonFatalError(@NonNull String troubleshootingMessage) {
            super(false, false, null, troubleshootingMessage);
        }
    }
