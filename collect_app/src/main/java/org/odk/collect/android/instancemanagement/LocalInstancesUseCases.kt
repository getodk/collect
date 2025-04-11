package org.odk.collect.android.instancemanagement

import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormNameUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalInstancesUseCases {
    @JvmOverloads
    @JvmStatic
    fun createInstanceFile(
        formName: String,
        instancesDir: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val sanitizedFormName = FormNameUtils.formatFilenameFromFormName(formName)

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
            .format(Date(clock()))

        val instanceDir = instancesDir + File.separator + sanitizedFormName + "_" + timestamp

        if (FileUtils.createFolder(instanceDir)) {
            return File(instanceDir + File.separator + sanitizedFormName + "_" + timestamp + ".xml")
        } else {
            Timber.e(Error("Error creating form instance file"))
            return null
        }
    }

    fun clone(
        instanceFile: File,
        instancesDir: String,
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): Long {
        val sourceInstance = instancesRepository.getOneByPath(instanceFile.absolutePath)!!
        val formName = formsRepository.getAllByFormIdAndVersion(
            sourceInstance.formId,
            sourceInstance.formVersion
        ).first().displayName
        val targetInstanceFile = copyInstanceDir(instanceFile, instancesDir, formName, clock)

        return instancesRepository.save(
            Instance.Builder(sourceInstance)
                .dbId(null)
                .status(Instance.STATUS_VALID)
                .instanceFilePath(targetInstanceFile.absolutePath)
                .build()
        ).dbId
    }

    private fun copyInstanceDir(
        sourceInstanceFile: File,
        instancesDir: String,
        formName: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File {
        val sourceInstanceDir = sourceInstanceFile.parentFile!!
        val targetInstanceFile = createInstanceFile(formName, instancesDir, clock)!!
        val targetInstanceDir = targetInstanceFile.parentFile!!

        sourceInstanceDir.copyRecursively(targetInstanceDir, true)
        File(targetInstanceDir, "${sourceInstanceFile.name}").renameTo(targetInstanceFile)

        return targetInstanceFile
    }
}
