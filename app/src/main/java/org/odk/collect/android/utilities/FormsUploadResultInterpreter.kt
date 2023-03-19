package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.errors.ErrorItem
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.localization.getLocalizedString

object FormsUploadResultInterpreter {
    fun getFailures(result: Map<Instance, FormUploadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.displayName,
            context.getLocalizedString(R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            it.value?.message ?: ""
        )
    }

    fun getNumberOfFailures(result: Map<Instance, FormUploadException?>) = result.count {
        it.value != null
    }

    fun allFormsUploadedSuccessfully(result: Map<Instance, FormUploadException?>) = result.values.all {
        it == null
    }
}
