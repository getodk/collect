package org.odk.collect.formstest

import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.TempFiles.createTempFile
import java.io.File

object InstanceUtils {

    @JvmStatic
    fun buildInstance(formId: String?, version: String?, instancesDir: String): Instance.Builder {
        return buildInstance(
            formId,
            version,
            "display name",
            Instance.STATUS_INCOMPLETE,
            null,
            instancesDir
        )
    }

    @JvmStatic
    fun buildInstance(
        formId: String?,
        version: String?,
        displayName: String?,
        status: String?,
        deletedDate: Long?,
        instancesDir: String
    ): Instance.Builder {
        val instanceFile = createInstanceDirAndFile(instancesDir)

        return Instance.Builder()
            .formId(formId)
            .formVersion(version)
            .displayName(displayName)
            .instanceFilePath(instanceFile.absolutePath)
            .status(status)
            .deletedDate(deletedDate)
    }

    @JvmStatic
    fun createInstanceDirAndFile(instancesDir: String): File {
        val instanceDir = File(instancesDir + File.separator + System.currentTimeMillis() + Math.random())
        instanceDir.mkdir()

        return createTempFile(instanceDir, "intance", ".xml")
    }
}
