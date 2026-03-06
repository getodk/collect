package org.odk.collect.entities.javarosa.finalization

data class InvalidEntity(
    val dataset: String,
    val id: String?,
    val label: String?
)
