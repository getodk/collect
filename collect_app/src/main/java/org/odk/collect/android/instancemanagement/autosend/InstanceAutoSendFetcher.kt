package org.odk.collect.android.instancemanagement.autosend

import android.app.Application
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.enums.StringIdEnumUtils.getAutoSend

object InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        application: Application,
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        settingsProvider: SettingsProvider
    ): List<Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        val autoSendSetting =
            settingsProvider.getUnprotectedSettings().getAutoSend(application)

        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)?.let { form ->
                form.shouldFormBeSentAutomatically(autoSendSetting != AutoSend.OFF)
            } ?: false
        }
    }
}
