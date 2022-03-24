package org.odk.collect.android.formlist

data class FormListItem(
    val formId: Long,
    val formName: String,
    val formVersion: String,
    val geometryPath: String,
    val dateOfCreation: Long,
    val dateOfLastUsage: Long
)
