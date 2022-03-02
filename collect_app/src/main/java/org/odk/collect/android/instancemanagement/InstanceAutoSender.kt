package org.odk.collect.android.instancemanagement

import android.content.Context
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.gdrive.GoogleAccountsManager
import org.odk.collect.android.gdrive.GoogleApiProvider
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstanceUploaderUtils
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class InstanceAutoSender(
    private val context: Context,
    private val changeLockProvider: ChangeLockProvider,
    private val notifier: Notifier,
    private val analytics: Analytics,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val googleAccountsManager: GoogleAccountsManager,
    private val googleApiProvider: GoogleApiProvider,
    private val permissionsProvider: PermissionsProvider,
    private val settingsProvider: SettingsProvider,
    private val instancesAppState: InstancesAppState
) {
    fun autoSendInstances(projectId: String?): Boolean {
        val formsRepository = formsRepositoryProvider.get(projectId)
        val instancesRepository = instancesRepositoryProvider.get(projectId)
        val generalSettings = settingsProvider.getUnprotectedSettings(projectId)
        val instanceSubmitter = InstanceSubmitter(
            analytics,
            formsRepository,
            googleAccountsManager,
            googleApiProvider,
            permissionsProvider,
            generalSettings
        )
        return changeLockProvider.getInstanceLock(projectId!!).withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val toUpload = getInstancesToAutoSend(
                    formsRepository,
                    instancesRepository,
                    generalSettings
                )
                try {
                    val result: Map<Instance, FormUploadException?> = instanceSubmitter.submitInstances(toUpload)
                    notifier.onSubmission(result, projectId)
                } catch (e: SubmitException) {
                    if (e.type == SubmitException.Type.GOOGLE_ACCOUNT_NOT_SET) {
                        val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                            FormUploadException(context.getString(R.string.google_set_account))
                        }
                        notifier.onSubmission(result, projectId)
                    } else if (e.type == SubmitException.Type.GOOGLE_ACCOUNT_NOT_PERMITTED) {
                        val result: Map<Instance, FormUploadException?> = toUpload.associateWith {
                            FormUploadException(context.getString(R.string.odk_permissions_fail))
                        }
                        notifier.onSubmission(result, projectId)
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
        val toUpload: MutableList<Instance> = ArrayList()
        for (instance in instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )) {
            if (InstanceUploaderUtils.shouldFormBeSent(
                    formsRepository,
                    instance.formId,
                    instance.formVersion,
                    isAutoSendAppSettingEnabled
                )
            ) {
                toUpload.add(instance)
            }
        }
        return toUpload
    }
}
