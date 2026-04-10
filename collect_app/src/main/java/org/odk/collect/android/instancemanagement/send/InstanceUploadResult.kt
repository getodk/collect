package org.odk.collect.android.instancemanagement.send

import android.content.res.Resources
import org.odk.collect.android.instancemanagement.userVisibleInstanceName
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.R

sealed class InstanceUploadResult {
    abstract val instance: Instance

    data class Success(override val instance: Instance, val message: String?) : InstanceUploadResult()

    data class Error(override val instance: Instance, val exception: FormUploadException) : InstanceUploadResult()
}

private const val DEFAULT_SUCCESSFUL_TEXT = "full submission upload was successful!"

fun List<InstanceUploadResult>.toMessage(resources: Resources): String {
    if (isEmpty()) {
        return resources.getString(R.string.no_forms_uploaded)
    }

    return joinToString(separator = "\n\n") { uploadResult ->
        val name = uploadResult.instance.userVisibleInstanceName(resources)

        val message = when (uploadResult) {
            is InstanceUploadResult.Success -> uploadResult.message
            is InstanceUploadResult.Error -> uploadResult.exception.message
        }?.let {
            if (it == DEFAULT_SUCCESSFUL_TEXT) {
                resources.getString(R.string.success)
            } else {
                it
            }
        } ?: ""

        "$name - $message"
    }
}
