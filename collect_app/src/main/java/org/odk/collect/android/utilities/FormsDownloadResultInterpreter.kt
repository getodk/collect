package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormDownloadException
import org.odk.collect.android.formmanagement.FormDownloadExceptionMapper
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.errors.ErrorItem
import org.odk.collect.strings.localization.getLocalizedString

object FormsDownloadResultInterpreter {
    fun getFailures(result: Map<ServerFormDetails, FormDownloadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.formName ?: "",
            context.getLocalizedString(R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            FormDownloadExceptionMapper(context).getMessage(it.value)
        )
    }

    fun getNumberOfFailures(result: Map<ServerFormDetails, FormDownloadException?>, context: Context) = result.count {
        it.value != null
    }

    fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, FormDownloadException?>, context: Context) = result.values.all {
        it == null
    }
}
