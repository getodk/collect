package org.odk.collect.android.formmanagement.download

import android.content.Context
import org.odk.collect.android.formmanagement.FormSourceExceptionMapper
import org.odk.collect.strings.localization.getLocalizedString

class FormDownloadExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormDownloadException?): String {
        return when (exception) {
            is FormDownloadException.FormWithNoHash -> {
                context.getLocalizedString(
                    org.odk.collect.strings.R.string.form_with_no_hash_error
                ) + " " + context.getLocalizedString(
                    org.odk.collect.strings.R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormParsingError -> {
                context.getLocalizedString(org.odk.collect.strings.R.string.form_parsing_error) +
                    "\n\n${exception.original.getStackTraceString(1)}\n\n" +
                    context.getLocalizedString(org.odk.collect.strings.R.string.report_to_project_lead)
            }
            is FormDownloadException.DiskError -> {
                context.getLocalizedString(
                    org.odk.collect.strings.R.string.form_save_disk_error
                ) + " " + context.getLocalizedString(
                    org.odk.collect.strings.R.string.report_to_project_lead
                )
            }
            is FormDownloadException.InvalidSubmission -> {
                context.getLocalizedString(
                    org.odk.collect.strings.R.string.form_with_invalid_submission_error
                ) + " " + context.getLocalizedString(
                    org.odk.collect.strings.R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormSourceError -> {
                FormSourceExceptionMapper(context).getMessage(exception.exception)
            }
            else -> {
                context.getLocalizedString(org.odk.collect.strings.R.string.report_to_project_lead)
            }
        }
    }

    private fun Exception.getStackTraceString(lines: Int): String {
        val stackTrace = this.stackTrace
        return "${this.message}\n${stackTrace.take(lines).joinToString("\n") { it.toString() }}"
    }
}
