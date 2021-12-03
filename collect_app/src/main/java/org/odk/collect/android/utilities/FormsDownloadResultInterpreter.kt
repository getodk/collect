package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.errors.ErrorItem
import org.odk.collect.strings.localization.getLocalizedString

class FormsDownloadResultInterpreter(private val context: Context) {
    fun getFailures(result: Map<ServerFormDetails, String>) = result.filter {
        it.value != context.getLocalizedString(R.string.success)
    }.map {
        ErrorItem(
            it.key.formName ?: "",
            context.getLocalizedString(R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            it.value
        )
    }

    fun getNumberOfFailures(result: Map<ServerFormDetails, String>) = result.count {
        it.value != context.getLocalizedString(R.string.success)
    }

    fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, String>) = result.values.all {
        it == context.getLocalizedString(R.string.success)
    }
}
