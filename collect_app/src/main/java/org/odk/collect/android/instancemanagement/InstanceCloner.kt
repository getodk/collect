package org.odk.collect.android.instancemanagement

import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.PathUtils.getRelativeFilePath
import java.io.File

class InstanceCloner(
    private val instancesRepository: InstancesRepository,
    private val instancesPath: String
) {
    fun clone(formController: FormController): Long? {
        val sourceInstanceFile = formController.getInstanceFile() ?: return null
        val targetInstanceFile = copyInstanceDir(sourceInstanceFile) ?: return null
        val sourceInstance = instancesRepository.getOneByPath(sourceInstanceFile.absolutePath) ?: return null

        return instancesRepository.save(
            Instance.Builder(sourceInstance)
                .dbId(null)
                .status(Instance.STATUS_VALID)
                .instanceFilePath(getRelativeFilePath(instancesPath, targetInstanceFile.absolutePath))
                .build()
        ).dbId
    }

    private fun copyInstanceDir(sourceInstanceFile: File): File? {
        val sourceInstanceDir = sourceInstanceFile.parentFile ?: return null
        val targetInstanceDir = File(sourceInstanceDir.parent, "${sourceInstanceDir.name}_1")

        if (!sourceInstanceDir.copyRecursively(targetInstanceDir, true)) return null

        val targetInstanceFile = File(targetInstanceDir, sourceInstanceFile.name)
        val updatedTargetInstanceFile = File(targetInstanceDir, "${sourceInstanceFile.nameWithoutExtension}_1.${sourceInstanceFile.extension}")

        return if (targetInstanceFile.renameTo(updatedTargetInstanceFile)) {
            updatedTargetInstanceFile
        } else {
            null
        }
    }
}
