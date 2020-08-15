package org.odk.collect.android.formmanagement;

public class FormDownloadException extends Exception {

    private final String resultMessage;

    /**
     * @deprecated Exception shouldn't pass strings back to UI
     */
    @Deprecated
    public FormDownloadException(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public FormDownloadException() {
        this.resultMessage = "";
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
