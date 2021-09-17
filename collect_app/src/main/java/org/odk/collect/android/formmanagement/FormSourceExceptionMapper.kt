package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.forms.FormSourceException

class FormSourceExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormSourceException?): String {
        return when (exception) {
            is FormSourceException.Unreachable -> {
                TranslationHandler.getString(
                    context,
                    R.string.unreachable_error,
                    exception.serverUrl
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.SecurityError -> {
                TranslationHandler.getString(
                    context,
                    R.string.security_error,
                    exception.serverUrl
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerError -> {
                TranslationHandler.getString(
                    context,
                    R.string.server_error,
                    exception.serverUrl,
                    exception.statusCode
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.ParseError -> {
                TranslationHandler.getString(
                    context,
                    R.string.invalid_response,
                    exception.serverUrl
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerNotOpenRosaError -> {
                "This server does not correctly implement the OpenRosa formList API." + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.FormWithNoHashException -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_with_no_hash_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.FormParsingException -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_parsing_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.DiskException -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_save_disk_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            is FormSourceException.InvalidSubmissionException -> {
                TranslationHandler.getString(
                    context,
                    R.string.form_with_invalid_submission_error
                ) + " " + TranslationHandler.getString(
                    context, R.string.report_to_project_lead
                )
            }
            else -> {
                TranslationHandler.getString(context, R.string.report_to_project_lead)
            }
        }
    }
}
