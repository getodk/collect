package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository

object InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        forcedOnly: Boolean = false
    ): List<Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        val filter: (Form) -> Boolean = if (forcedOnly) {
            { form -> form.getAutoSendMode() == FormAutoSendMode.FORCED }
        } else {
            { form -> form.getAutoSendMode() == FormAutoSendMode.NEUTRAL }
        }

        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)
                ?.let { form -> filter(form) } ?: false
        }
    }
}
