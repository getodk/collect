package org.odk.collect.android.instancemanagement

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.Collect
import org.odk.collect.android.instancemanagement.send.FormUploadException
import org.odk.collect.android.instancemanagement.send.ServerInstanceUploader
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstanceAutoDeleteChecker
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.metadata.PropertyManager.Companion.PROPMGR_DEVICE_ID
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import timber.log.Timber

class InstanceSubmitter(
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val propertyManager: PropertyManager,
    private val httpInterface: OpenRosaHttpInterface
) {

    fun submitInstances(projectId: String, toUpload: List<Instance>): Map<Instance, FormUploadException?> {
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val formsRepository = projectDependencyModule.formsRepository
        val instancesRepository = projectDependencyModule.instancesRepository
        val generalSettings = projectDependencyModule.generalSettings

        val result = mutableMapOf<Instance, FormUploadException?>()
        val deviceId = propertyManager.getSingularProperty(PROPMGR_DEVICE_ID)

        val uploader = setUpODKUploader(instancesRepository, generalSettings)

        for (instance in toUpload.sortedBy { it.finalizationDate }) {
            try {
                uploader.uploadOneSubmission(instance, deviceId, null)
                result[instance] = null

                deleteInstance(instance, formsRepository, generalSettings)
                logUploadedForm(formsRepository, instance)
            } catch (e: FormUploadException) {
                Timber.d(e)
                result[instance] = e
            }
        }
        return result
    }

    private fun setUpODKUploader(instancesRepository: InstancesRepository, generalSettings: Settings): ServerInstanceUploader {
        return ServerInstanceUploader(
            httpInterface,
            WebCredentialsUtils(generalSettings),
            generalSettings,
            instancesRepository
        )
    }

    private fun deleteInstance(instance: Instance, formsRepository: FormsRepository, generalSettings: Settings) {
        // If the submission was successful, delete the instance if either the app-level
        // delete preference is set or the form definition requests auto-deletion.
        // TODO: this could take some time so might be better to do in a separate process,
        // perhaps another worker. It also feels like this could fail and if so should be
        // communicated to the user. Maybe successful delete should also be communicated?
        if (InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND), instance)) {
            InstanceDeleter(
                InstancesRepositoryProvider(Collect.getInstance()).create(),
                FormsRepositoryProvider(Collect.getInstance()).create()
            ).delete(instance.dbId)
        }
    }

    private fun logUploadedForm(formsRepository: FormsRepository, instance: Instance) {
        val value = Collect.getFormIdentifierHash(formsRepository, instance.formId, instance.formVersion)

        Analytics.log(AnalyticsEvents.SUBMISSION, "HTTP auto", value)
    }
}
