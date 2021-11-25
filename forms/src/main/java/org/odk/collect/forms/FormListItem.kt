package org.odk.collect.forms

data class FormListItem(
    val downloadURL: String,
    val formID: String,
    val version: String?,
    val hash: String?,
    val name: String,
    val manifestURL: String?
)
