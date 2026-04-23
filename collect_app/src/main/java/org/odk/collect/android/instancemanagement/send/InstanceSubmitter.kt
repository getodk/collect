package org.odk.collect.android.instancemanagement.send

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.InstanceAutoDeleteChecker
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.metadata.PropertyManager.Companion.PROPMGR_DEVICE_ID
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import timber.log.Timber

class InstanceSubmitter(
    private val instanceUploader: InstanceUploader,
    private val projectDependencyFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val propertyManager: PropertyManager
) {

    fun submitInstances(
        projectId: String,
        toUpload: List<Instance>
    ): List<InstanceUploadResult> {
        val projectDependencyModule = projectDependencyFactory.create(projectId)
        val formsRepository = projectDependencyModule.formsRepository
        val instancesRepository = projectDependencyModule.instancesRepository
        val generalSettings = projectDependencyModule.generalSettings

        val uploadResults = mutableListOf<InstanceUploadResult>()
        val deviceId = propertyManager.getSingularProperty(PROPMGR_DEVICE_ID)

        val sortedInstances = toUpload.sortedBy { it.finalizationDate }
        for (instance in sortedInstances) {
            try {
                val resultMessage = instanceUploader.uploadOneSubmission(projectId, instance, deviceId, null, "")
                uploadResults.add(InstanceUploadResult.Success(instance, resultMessage))

                deleteInstance(instance, formsRepository, instancesRepository, generalSettings, null)
            } catch (e: FormUploadException) {
                Timber.d(e)
                uploadResults.add(InstanceUploadResult.Error(instance, e))

                if (e is FormUploadAuthRequestedException) {
                    break
                }
            }
        }

        return uploadResults
    }

    suspend fun submitInstances(
        projectId: String,
        toUpload: List<Instance>,
        referrer: String,
        overrideURL: String?,
        cancelAfterAuthException: Boolean,
        externalDeleteAfterUpload: Boolean?,
        defaultSuccessMessage: String?,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<InstanceUploadResult> {
        val projectDependencyModule = projectDependencyFactory.create(projectId)
        val formsRepository = projectDependencyModule.formsRepository
        val instancesRepository = projectDependencyModule.instancesRepository
        val generalSettings = projectDependencyModule.generalSettings

        val uploadResults = mutableListOf<InstanceUploadResult>()
        val deviceId = propertyManager.getSingularProperty(PROPMGR_DEVICE_ID)

        val sortedInstances = toUpload.sortedBy { it.finalizationDate }
        for ((index, instance) in sortedInstances.withIndex()) {
            currentCoroutineContext().ensureActive()
            onProgress(index + 1, sortedInstances.size)

            try {
                val resultMessage = runInterruptible {
                    instanceUploader.uploadOneSubmission(projectId, instance, deviceId, overrideURL, referrer)
                }
                uploadResults.add(InstanceUploadResult.Success(instance, resultMessage ?: defaultSuccessMessage))
                deleteInstance(instance, formsRepository, instancesRepository, generalSettings, externalDeleteAfterUpload)
            } catch (e: FormUploadException) {
                Timber.d(e)
                uploadResults.add(InstanceUploadResult.Error(instance, e))

                if (e is FormUploadAuthRequestedException && cancelAfterAuthException) {
                    break
                }
            }
        }

        return uploadResults
    }

    private fun deleteInstance(
        instance: Instance,
        formsRepository: FormsRepository,
        instancesRepository: InstancesRepository,
        generalSettings: Settings,
        externalDeleteAfterUpload: Boolean?
    ) {
        // If the submission was successful, delete the instance if either the app-level
        // delete preference is set or the form definition requests auto-deletion.
        // TODO: this could take some time so might be better to do in a separate process,
        // perhaps another worker. It also feels like this could fail and if so should be
        // communicated to the user. Maybe successful delete should also be communicated?
        val isFormAutoDeleteOptionEnabled = externalDeleteAfterUpload ?: generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND)

        if (InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, isFormAutoDeleteOptionEnabled, instance)) {
            InstanceDeleter(
                instancesRepository,
                formsRepository
            ).delete(instance.dbId)
        }
    }
}
