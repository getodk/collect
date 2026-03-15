package org.odk.collect.android.instancemanagement.send

import org.odk.collect.forms.instances.Instance

interface InstanceUploader {
    @Throws(FormUploadException::class)
    fun uploadOneSubmission(projectId: String, instance: Instance, deviceId: String?, overrideURL: String?): String?
}
