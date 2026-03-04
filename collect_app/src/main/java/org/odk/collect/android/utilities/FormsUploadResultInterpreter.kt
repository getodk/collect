package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.instancemanagement.InstanceUploadResult
import org.odk.collect.android.instancemanagement.userVisibleInstanceName
import org.odk.collect.errors.ErrorItem
import org.odk.collect.strings.localization.getLocalizedString

object FormsUploadResultInterpreter {
    fun getFailures(uploadResults: List<InstanceUploadResult>, context: Context) = uploadResults.filter {
        it is InstanceUploadResult.Error
    }.map {
        ErrorItem(
            it.instance.userVisibleInstanceName(context.resources),
            context.getLocalizedString(org.odk.collect.strings.R.string.form_details, it.instance.formId ?: "", it.instance.formVersion ?: ""),
            (it as InstanceUploadResult.Error).exception.message
        )
    }

    fun getNumberOfFailures(uploadResults: List<InstanceUploadResult>) = uploadResults.count {
        it is InstanceUploadResult.Error
    }

    fun allFormsUploadedSuccessfully(uploadResults: List<InstanceUploadResult>) = uploadResults.all {
        it is InstanceUploadResult.Success
    }
}
