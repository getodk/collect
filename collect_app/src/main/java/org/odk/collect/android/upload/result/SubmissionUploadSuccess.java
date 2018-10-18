package org.odk.collect.android.upload.result;

import android.support.annotation.NonNull;

import org.odk.collect.android.R;

public class SubmissionUploadSuccess extends SubmissionUploadResult {
    public SubmissionUploadSuccess() {
        super(true, false, R.string.success, "Success");
    }

    public SubmissionUploadSuccess(@NonNull String serverMessage) {
        super(true, false, null, serverMessage);
    }
}