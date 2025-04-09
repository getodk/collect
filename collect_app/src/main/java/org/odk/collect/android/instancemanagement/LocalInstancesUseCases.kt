package org.odk.collect.android.instancemanagement

import org.odk.collect.android.formentry.loading.FormInstanceFileCreator
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.PathUtils.getRelativeFilePath
import java.io.File

object LocalInstancesUseCases {
    fun clone(
        instanceFile: File?,
        instancesDir: String,
        instancesRepository: InstancesRepository,
        formInstanceFileCreator: FormInstanceFileCreator
    ): Long? {
        val sourceInstanceFile = instanceFile ?: return null
        val targetInstanceFile = copyInstanceDir(sourceInstanceFile, formInstanceFileCreator) ?: return null
        val sourceInstance = instancesRepository.getOneByPath(sourceInstanceFile.absolutePath) ?: return null

        return instancesRepository.save(
            Instance.Builder(sourceInstance)
                .dbId(null)
                .status(Instance.STATUS_VALID)
                .instanceFilePath(
                    getRelativeFilePath(instancesDir, targetInstanceFile.absolutePath)
                )
                .build()
        ).dbId
    }

    private fun copyInstanceDir(
        sourceInstanceFile: File,
        formInstanceFileCreator: FormInstanceFileCreator
    ): File? {
        val sourceInstanceDir = sourceInstanceFile.parentFile ?: return null
        val targetInstanceFile = formInstanceFileCreator.createInstanceFileBasedOnInstanceName(sourceInstanceFile.nameWithoutExtension) ?: return null
        val targetInstanceDir = targetInstanceFile.parentFile ?: return null

        if (!sourceInstanceDir.copyRecursively(targetInstanceDir, true)) return null

        return if (File(targetInstanceDir, "${sourceInstanceFile.name}").renameTo(targetInstanceFile)) {
            targetInstanceFile
        } else {
            null
        }
    }
}
