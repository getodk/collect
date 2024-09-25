package org.odk.collect.android.formmanagement.metadata

data class FormMetadata(
    val title: String?,
    val id: String?,
    val version: String?,
    val submissionUri: String?,
    val base64RsaPublicKey: String?,
    val autoDelete: String?,
    val autoSend: String?,
    val geometryXPath: String?
)
