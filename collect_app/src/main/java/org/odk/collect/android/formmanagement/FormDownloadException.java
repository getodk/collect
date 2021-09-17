package org.odk.collect.android.formmanagement;

public class FormDownloadException extends Exception {
    public FormDownloadException() {
        super();
    }

    public FormDownloadException(String message) {
        super(message);
    }

    public static class DownloadingInterruptedException extends FormDownloadException {

    }

    public static class FormWithNoHashException extends FormDownloadException {

    }

    public static class FormParsingException extends FormDownloadException {

    }

    public static class DiskException extends FormDownloadException {

    }
}
