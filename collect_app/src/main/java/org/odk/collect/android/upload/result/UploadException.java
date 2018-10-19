package org.odk.collect.android.upload.result;

/**
 * An exception that results in the cancellation of an instance upload, and the presentation of an
 * error to the user
 * */
public class UploadException extends Exception {
    public UploadException(String message) {
        super(message);
    }

    public UploadException(Throwable cause) {
        super(cause);
    }
}