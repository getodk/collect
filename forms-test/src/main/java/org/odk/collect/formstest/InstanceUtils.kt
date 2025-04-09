package org.odk.collect.formstest

import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.TempFiles.createTempFile
import org.odk.collect.shared.strings.RandomString
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
            .format(Date((100_000_000_0000L..999_999_999_9999L).random()))

        val instanceDir = File(instancesDir + File.separator + RandomString.randomString(5) + "_" + timestamp)
        instanceDir.mkdir()

        return createTempFile(instanceDir, instanceDir.name, ".xml").also {
            it.writeText(RandomString.randomString(10))
        }
    }
}
