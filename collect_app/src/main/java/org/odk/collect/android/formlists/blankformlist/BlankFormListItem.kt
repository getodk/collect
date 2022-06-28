package org.odk.collect.android.formlists.blankformlist

import android.net.Uri

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
