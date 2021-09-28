package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.strings.localization.getLocalizedString

class FormDownloadExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormDownloadException?): String {
        return when (exception) {
            is FormDownloadException.FormWithNoHash -> {
                context.getLocalizedString(
                    R.string.form_with_no_hash_error
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormParsingError -> {
                context.getLocalizedString(
                    R.string.form_parsing_error
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormDownloadException.DiskError -> {
                context.getLocalizedString(
                    R.string.form_save_disk_error
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormDownloadException.InvalidSubmission -> {
                context.getLocalizedString(
                    R.string.form_with_invalid_submission_error
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormSourceError -> {
                FormSourceExceptionMapper(context).getMessage(exception.exception)
            }
            else -> {
                context.getLocalizedString(R.string.report_to_project_lead)
            }
        }
    }
}
