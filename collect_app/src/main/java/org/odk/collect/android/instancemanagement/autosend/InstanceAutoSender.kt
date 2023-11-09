package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.android.formmanagement.InstancesDataService
import org.odk.collect.android.instancemanagement.InstanceSubmitter
import org.odk.collect.android.instancemanagement.SubmitException
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.forms.instances.Instance
import org.odk.collect.metadata.PropertyManager

class InstanceAutoSender(
    private val instanceAutoSendFetcher: InstanceAutoSendFetcher,
    private val notifier: Notifier,
    private val instancesDataService: InstancesDataService,
    private val propertyManager: PropertyManager
) {
    fun autoSendInstances(projectDependencyProvider: ProjectDependencyProvider): Boolean {
        val instanceSubmitter = InstanceSubmitter(
            projectDependencyProvider.formsRepository,
            projectDependencyProvider.generalSettings,
            propertyManager
        )
        return projectDependencyProvider.changeLockProvider.getInstanceLock(projectDependencyProvider.projectId).withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val toUpload = instanceAutoSendFetcher.getInstancesToAutoSend(
                    projectDependencyProvider.projectId,
                    projectDependencyProvider.instancesRepository,
                    projectDependencyProvider.formsRepository
                )

                try {
                    val result: Map<Instance, FormUploadException?> = instanceSubmitter.submitInstances(toUpload)
                    notifier.onSubmission(result, projectDependencyProvider.projectId)
                } catch (e: SubmitException) {
                    // do nothing
                }
                instancesDataService.update()
                true
            } else {
                false
            }
        }
    }
}
