package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.forms.FormSourceException
import org.odk.collect.strings.localization.getLocalizedString

class FormSourceExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormSourceException?): String {
        return when (exception) {
            is FormSourceException.Unreachable -> {
                context.getLocalizedString(
                    R.string.unreachable_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormSourceException.SecurityError -> {
                context.getLocalizedString(
                    R.string.security_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerError -> {
                context.getLocalizedString(
                    R.string.server_error,
                    exception.serverUrl,
                    exception.statusCode
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormSourceException.ParseError -> {
                context.getLocalizedString(
                    R.string.invalid_response,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerNotOpenRosaError -> {
                "This server does not correctly implement the OpenRosa formList API." + " " + context.getLocalizedString(
                    R.string.report_to_project_lead
                )
            }
            else -> {
                context.getLocalizedString(R.string.report_to_project_lead)
            }
        }
    }
}
