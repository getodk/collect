package org.odk.collect.android.formlists.blankformlist

import android.net.Uri
import org.odk.collect.android.external.FormsContract
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.InstancesRepository

data class BlankFormListItem(
    val databaseId: Long,
    val formId: String,
    val formName: String,
    val formVersion: String,
    val geometryPath: String,
    val dateOfCreation: Long,
    val dateOfLastUsage: Long,
    val contentUri: Uri
)

fun formToBlankFormListItem(form: Form, projectId: String, instancesRepository: InstancesRepository) = BlankFormListItem(
    databaseId = form.dbId,
    formId = form.formId,
    formName = form.displayName,
    formVersion = form.version ?: "",
    geometryPath = form.geometryXpath ?: "",
    dateOfCreation = form.date,
    dateOfLastUsage = instancesRepository
        .getAllByFormId(form.formId)
        .filter { it.formVersion == form.version }
        .maxByOrNull { it.lastStatusChangeDate }?.lastStatusChangeDate ?: 0L,
    contentUri = FormsContract.getUri(projectId, form.dbId)
)
