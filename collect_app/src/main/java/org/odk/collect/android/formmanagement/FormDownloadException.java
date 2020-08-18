package org.odk.collect.android.formmanagement;

public class FormDownloadException extends Exception {

    /**
     * @deprecated Exception shouldn't pass strings back to UI
     */
    @Deprecated
    public FormDownloadException(String resultMessage) {
        super(resultMessage);
    }

    public FormDownloadException() {
        super();
    }
}
