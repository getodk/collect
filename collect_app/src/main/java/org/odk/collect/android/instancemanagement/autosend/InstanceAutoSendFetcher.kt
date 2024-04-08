package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository

class InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository
    ): List<Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)?.let { form ->
                form.shouldFormBeSentAutomatically(true)
            } ?: false
        }
    }
}
