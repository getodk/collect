package org.odk.collect.android.instancemanagement

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
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
import java.util.TimeZone

object LocalInstancesUseCases {
    @JvmOverloads
    @JvmStatic
    fun createInstanceFile(
        formName: String,
        instancesDir: String,
        timezone: TimeZone = TimeZone.getDefault(),
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val sanitizedFormName = FormNameUtils.formatFilenameFromFormName(formName)

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).also {
            it.timeZone = timezone
        }.format(Date(clock()))

        val instanceDir = instancesDir + File.separator + sanitizedFormName + "_" + timestamp

        if (FileUtils.createFolder(instanceDir)) {
            return File(instanceDir + File.separator + sanitizedFormName + "_" + timestamp + ".xml")
        } else {
            Timber.e(Error("Error creating form instance file"))
            return null
        }
    }

    fun editInstance(
        instanceFilePath: String,
        instancesDir: String,
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): InstanceEditResult {
        val sourceInstance = instancesRepository.getOneByPath(instanceFilePath)!!

        val latestEditInstance = findLatestEditIfExists(sourceInstance, instancesRepository)
        if (latestEditInstance != null) {
            return InstanceEditResult.EditBlockedByNewerExistingEdit(latestEditInstance)
        }

        val formHash = Analytics.getParamValue("form")
        val actionValue = if (sourceInstance.status == Instance.STATUS_COMPLETE) {
            "finalized $formHash"
        } else {
            "sent $formHash"
        }
        Analytics.log(AnalyticsEvents.EDIT_FINALIZED_OR_SENT_FORM, "action", actionValue)

        val targetInstance = cloneInstance(sourceInstance, instanceFilePath, instancesDir, instancesRepository, formsRepository, clock)

        return InstanceEditResult.EditCompleted(targetInstance)
    }

    private fun findLatestEditIfExists(
        instance: Instance,
        instancesRepository: InstancesRepository
    ): Instance? {
        val editGroupId = if (instance.isEdit()) {
            instance.editOf
        } else {
            instance.dbId
        }

        return instancesRepository
            .all
            .filter { it.editOf == editGroupId }
            .maxByOrNull { it.editNumber!! }
            ?.takeIf { it.dbId != instance.dbId }
    }

    private fun cloneInstance(
        sourceInstance: Instance,
        instanceFilePath: String,
        instancesDir: String,
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): Instance {
        val formName = formsRepository.getAllByFormIdAndVersion(
            sourceInstance.formId,
            sourceInstance.formVersion
        ).first().displayName
        val targetInstanceFile = copyInstanceDir(File(instanceFilePath), instancesDir, formName, clock)

        return instancesRepository.save(
            Instance.Builder(sourceInstance)
                .dbId(null)
                .status(Instance.STATUS_NEW_EDIT)
                .instanceFilePath(targetInstanceFile.absolutePath)
                .editOf(sourceInstance.editOf ?: sourceInstance.dbId)
                .editNumber((sourceInstance.editNumber ?: 0) + 1)
                .build()
        )
    }

    private fun copyInstanceDir(
        sourceInstanceFile: File,
        instancesDir: String,
        formName: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File {
        val sourceInstanceDir = sourceInstanceFile.parentFile!!
        val targetInstanceFile = createInstanceFile(formName, instancesDir, clock = clock)!!
        val targetInstanceDir = targetInstanceFile.parentFile!!

        sourceInstanceDir.copyRecursively(targetInstanceDir, true)
        File(targetInstanceDir, sourceInstanceFile.name).renameTo(targetInstanceFile)

        return targetInstanceFile
    }
}

sealed class InstanceEditResult(val instance: Instance) {
    data class EditCompleted(val resultInstance: Instance) : InstanceEditResult(resultInstance)
    data class EditBlockedByNewerExistingEdit(val resultInstance: Instance) : InstanceEditResult(resultInstance)
}
