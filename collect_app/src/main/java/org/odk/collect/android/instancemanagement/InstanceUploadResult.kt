package org.odk.collect.android.instancemanagement

import org.odk.collect.android.instancemanagement.send.FormUploadException
import org.odk.collect.forms.instances.Instance

sealed class InstanceUploadResult {
    abstract val instance: Instance

    data class Success(override val instance: Instance, val message: String?) : InstanceUploadResult()

    data class Error(override val instance: Instance, val exception: FormUploadException) : InstanceUploadResult()
}
