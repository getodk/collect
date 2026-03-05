package org.odk.collect.android.instancemanagement

import android.content.res.Resources
import org.odk.collect.android.instancemanagement.send.FormUploadException
import org.odk.collect.forms.instances.Instance

sealed class InstanceUploadResult {
    abstract val instance: Instance

    data class Success(override val instance: Instance, val message: String?) : InstanceUploadResult()

    data class Error(override val instance: Instance, val exception: FormUploadException) : InstanceUploadResult()
}

private const val DEFAULT_SUCCESSFUL_TEXT = "full submission upload was successful!"

fun List<InstanceUploadResult>.toMessage(resources: Resources): String {
    if (isEmpty()) {
        return resources.getString(org.odk.collect.strings.R.string.no_forms_uploaded)
    }

    return joinToString(separator = "\n\n") { uploadResult ->
        val name = uploadResult.instance.userVisibleInstanceName(resources)

        val message = when (uploadResult) {
            is InstanceUploadResult.Success -> uploadResult.message
            is InstanceUploadResult.Error -> uploadResult.exception.message
        }?.let {
            if (it == DEFAULT_SUCCESSFUL_TEXT) {
                resources.getString(org.odk.collect.strings.R.string.success)
            } else {
                it
            }
        } ?: ""

        "$name - $message"
    }
}
