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
            // If there's more than one form with the same formid/version, trust the user that they want to truly delete this one
            // because otherwise it may not ever be removed (instance deletion only deletes one corresponding form).
            val formsWithSameFormIdVersion = formsRepository.getAllByFormIdAndVersion(
                form.formId, form.version
            )
            if (instancesForVersion.isEmpty() || formsWithSameFormIdVersion.size > 1) {
                formsRepository.delete(id)
            } else {
                formsRepository.softDelete(form.dbId)
            }
        }
    }
}
