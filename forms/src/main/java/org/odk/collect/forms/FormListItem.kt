package org.odk.collect.forms

data class FormListItem(
    val downloadURL: String,
    val formID: String,
    val version: String,
    private val hashWithPrefix: String?,
    val name: String,
    val manifestURL: String?
) {

    val hash = hashWithPrefix?.substring("md5:".length)
}
