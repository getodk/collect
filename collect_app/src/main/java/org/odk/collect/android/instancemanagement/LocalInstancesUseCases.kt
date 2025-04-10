package org.odk.collect.android.instancemanagement

import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LocalInstancesUseCases {
    @JvmOverloads
    @JvmStatic
    fun createInstanceFileBasedOnFormPath(
        formDefinitionPath: String,
        instancesDir: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val formFileName = File(formDefinitionPath).nameWithoutExtension

        return createInstanceFile(
            formFileName,
            instancesDir,
            clock
        )
    }

    fun clone(
        instanceFile: File,
        instancesDir: String,
        instancesRepository: InstancesRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): Long? {
        val targetInstanceFile = copyInstanceDir(instanceFile, instancesDir, clock) ?: return null
        val sourceInstance = instancesRepository.getOneByPath(instanceFile.absolutePath) ?: return null

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
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val sourceInstanceDir = sourceInstanceFile.parentFile ?: return null
        val targetInstanceFile = createInstanceFileBasedOnInstanceName(sourceInstanceFile.nameWithoutExtension, instancesDir, clock) ?: return null
        val targetInstanceDir = targetInstanceFile.parentFile ?: return null

        if (!sourceInstanceDir.copyRecursively(targetInstanceDir, true)) return null

        return if (File(targetInstanceDir, "${sourceInstanceFile.name}").renameTo(targetInstanceFile)) {
            targetInstanceFile
        } else {
            null
        }
    }

    private fun createInstanceFileBasedOnInstanceName(
        instanceFileName: String,
        instancesDir: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val index = instanceFileName.lastIndexOf('_')
        val secondToLastIndex = instanceFileName.lastIndexOf('_', index - 1)

        val baseName = if (secondToLastIndex != -1) {
            instanceFileName.substring(0, secondToLastIndex)
        } else {
            instanceFileName
        }

        return createInstanceFile(baseName, instancesDir, clock)
    }

    private fun createInstanceFile(
        baseName: String,
        instancesDir: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = dateFormat.format(Date(clock()))

        val instanceDir = instancesDir + File.separator + baseName + "_" + timestamp

        if (FileUtils.createFolder(instanceDir)) {
            return File(instanceDir + File.separator + baseName + "_" + timestamp + ".xml")
        } else {
            Timber.e(Error("Error creating form instance file"))
            return null
        }
    }
}
