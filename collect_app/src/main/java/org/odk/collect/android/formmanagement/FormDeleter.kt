package org.odk.collect.android.formmanagement

import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository

class FormDeleter(
    private val formsRepository: FormsRepository,
    private val instancesRepository: InstancesRepository
) {
    fun delete(id: Long) {
        formsRepository[id]?.let { form ->
            val instancesForVersion = instancesRepository.getAllNotDeletedByFormIdAndVersion(
                form.formId, form.version
            )
            if (instancesForVersion.isEmpty()) {
                formsRepository.delete(id)
            } else {
                formsRepository.softDelete(form.dbId)
            }
        }
    }
}
