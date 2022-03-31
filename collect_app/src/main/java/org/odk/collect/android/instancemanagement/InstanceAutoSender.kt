package org.odk.collect.android.instancemanagement

import android.content.Context
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.formmanagement.ProjectDependencyProvider
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.gdrive.GoogleApiProvider
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.android.utilities.InstanceUploaderUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class InstanceAutoSender(
    private val context: Context,
    private val notifier: Notifier,
    private val analytics: Analytics,
    private val googleAccountsManager: GoogleAccountsManager,
    private val googleApiProvider: GoogleApiProvider,
    private val permissionsProvider: PermissionsProvider,
    private val instancesAppState: InstancesAppState
) {
    fun autoSendInstances(projectDependencyProvider: ProjectDependencyProvider): Boolean {
        val instanceSubmitter = InstanceSubmitter(
            analytics,
            projectDependencyProvider.formsRepository,
            googleAccountsManager,
            googleApiProvider,
            permissionsProvider,
            projectDependencyProvider.generalSettings
        )
        return projectDependencyProvider.changeLockProvider.getInstanceLock(projectDependencyProvider.projectId).withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val toUpload = getInstancesToAutoSend(
                    projectDependencyProvider.formsRepository,
                    projectDependencyProvider.instancesRepository,
                    projectDependencyProvider.generalSettings
                )
                try {
                    val result: Map<Instance, FormUploadException?> = instanceSubmitter.submitInstances(toUpload)
                    notifier.onSubmission(result, projectDependencyProvider.projectId)
                } catch (e: SubmitException) {
                    when (e.type) {
                        SubmitException.Type.GOOGLE_ACCOUNT_NOT_SET -> {
                            val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                                FormUploadException(context.getString(R.string.google_set_account))
                            }
                            notifier.onSubmission(result, projectDependencyProvider.projectId)
                        }
                        SubmitException.Type.GOOGLE_ACCOUNT_NOT_PERMITTED -> {
                            val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                                FormUploadException(context.getString(R.string.odk_permissions_fail))
                            }
                            notifier.onSubmission(result, projectDependencyProvider.projectId)
                        }
                        SubmitException.Type.NOTHING_TO_SUBMIT -> {
                            // do nothing
                        }
                    }
                }
                instancesAppState.update()
                true
            } else {
                false
            }
        }
    }

    private fun getInstancesToAutoSend(
        formsRepository: FormsRepository,
        instancesRepository: InstancesRepository,
        generalSettings: Settings
    ): List<Instance> {
        val isAutoSendAppSettingEnabled = generalSettings.getString(ProjectKeys.KEY_AUTOSEND) != "off"
        return instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        ).filter {
            InstanceUploaderUtils.shouldFormBeSent(
                formsRepository,
                it.formId,
                it.formVersion,
                isAutoSendAppSettingEnabled
            )
        }
    }
}
