package org.odk.collect.android.formmanagement

import org.odk.collect.forms.FormSourceException

sealed class FormDownloadException : Exception() {
    class DownloadingInterrupted : FormDownloadException()
    class FormWithNoHash : FormDownloadException()
    class FormParsingError : FormDownloadException()
    class DiskError : FormDownloadException()
    class InvalidSubmission : FormDownloadException()
    class FormSourceError(val exception: FormSourceException) : FormDownloadException()
}
