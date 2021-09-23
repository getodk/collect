package org.odk.collect.android.logic

import java.io.Serializable

data class FormDownloadErrorItem(
    val formName: String,
    val formId: String,
    val formVersion: String,
    val errorMessage: String
) : Serializable
