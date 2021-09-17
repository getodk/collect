package org.odk.collect.android.formmanagement;

import org.odk.collect.forms.FormSourceException;

public class FormDownloadException extends FormSourceException {

    public static class DownloadingInterruptedException extends FormDownloadException {

    }

    public static class FormWithNoHashException extends FormDownloadException {

    }

    public static class FormParsingException extends FormDownloadException {

    }

    public static class DiskException extends FormDownloadException {

    }

    public static class InvalidSubmissionException extends FormDownloadException {

    }
}
