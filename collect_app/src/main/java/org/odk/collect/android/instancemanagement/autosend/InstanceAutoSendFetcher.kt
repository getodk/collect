package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository

object InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        instancesRepository: InstancesRepository,
        formsRepository: FormsRepository,
        formAutoSend: Boolean = false
    ): List<Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        val filter: (Form) -> Boolean = if (formAutoSend) {
            { form -> form.autoSend != null && form.autoSend == "true" }
        } else {
            { form -> form.autoSend == null }
        }

        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)
                ?.let { form -> filter(form) } ?: false
        }
    }
}
