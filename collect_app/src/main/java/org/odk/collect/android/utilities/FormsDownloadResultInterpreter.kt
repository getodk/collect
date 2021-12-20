package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.errors.ErrorItem
import org.odk.collect.strings.localization.getLocalizedString

object FormsDownloadResultInterpreter {
    fun getFailures(result: Map<ServerFormDetails, String>, context: Context) = result.filter {
        it.value != context.getLocalizedString(R.string.success)
    }.map {
        ErrorItem(
            it.key.formName ?: "",
            context.getLocalizedString(R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            it.value
        )
    }

    fun getNumberOfFailures(result: Map<ServerFormDetails, String>, context: Context) = result.count {
        it.value != context.getLocalizedString(R.string.success)
    }

    fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, String>, context: Context) = result.values.all {
        it == context.getLocalizedString(R.string.success)
    }
}
