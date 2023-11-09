package org.odk.collect.android.instancemanagement.autosend

import android.content.Context
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
    private val context: Context,
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
                    when (e.type) {
                        SubmitException.Type.GOOGLE_ACCOUNT_NOT_SET -> {
                            val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                                FormUploadException(context.getString(org.odk.collect.strings.R.string.google_set_account))
                            }
                            notifier.onSubmission(result, projectDependencyProvider.projectId)
                        }
                        SubmitException.Type.GOOGLE_ACCOUNT_NOT_PERMITTED -> {
                            val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                                FormUploadException(context.getString(org.odk.collect.strings.R.string.odk_permissions_fail))
                            }
                            notifier.onSubmission(result, projectDependencyProvider.projectId)
                        }
                        SubmitException.Type.NOTHING_TO_SUBMIT -> {
                            // do nothing
                        }
                    }
                }
                instancesDataService.update()
                true
            } else {
                false
            }
        }
    }
}
