package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.utilities.TranslationHandler

class FormDownloadExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormDownloadException?): String {
        return when (exception) {
            is FormDownloadException.FormWithNoHash -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_with_no_hash_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormParsingError -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_parsing_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormDownloadException.DiskError -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_save_disk_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormDownloadException.InvalidSubmission -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_with_invalid_submission_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormDownloadException.FormSourceError -> {
                FormSourceExceptionMapper(context).getMessage(exception.exception)
            }
            else -> {
                TranslationHandler.getString(context, R.string.report_to_project_lead)
            }
        }
    }
}
