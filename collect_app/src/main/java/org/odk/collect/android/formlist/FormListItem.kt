package org.odk.collect.android.formlist

import android.net.Uri

data class FormListItem(
    val databaseId: Long,
    val formId: String,
    val formName: String,
    val formVersion: String,
    val geometryPath: String,
    val dateOfCreation: Long,
    val dateOfLastUsage: Long,
    val contentUri: Uri
)
