package org.odk.collect.android.instancemanagement

import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository

class InstanceDeleter(
    private val instancesRepository: InstancesRepository,
    private val formsRepository: FormsRepository
) {
    fun delete(id: Long?) {
        instancesRepository[id]?.let { instance ->
            if (instance.status == Instance.STATUS_SUBMITTED) {
                instancesRepository.deleteWithLogging(id)
            } else {
                instancesRepository.delete(id)
            }
            val form =
                formsRepository.getLatestByFormIdAndVersion(instance.formId, instance.formVersion)
            if (form != null && form.isDeleted) {
                val otherInstances = instancesRepository.getAllNotDeletedByFormIdAndVersion(
                    form.formId,
                    form.version
                )
                if (otherInstances.isEmpty()) {
                    formsRepository.delete(form.dbId)
                }
            }
        }
    }
}
