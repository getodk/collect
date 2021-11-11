package org.odk.collect.forms

data class FormListItem(
    val downloadURL: String,
    val formID: String,
    val version: String,
    val hashWithPrefix: String?,
    val name: String,
    val manifestURL: String?
)
