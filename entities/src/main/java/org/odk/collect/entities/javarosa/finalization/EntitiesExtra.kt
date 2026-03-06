package org.odk.collect.entities.javarosa.finalization

data class EntitiesExtra(
    val entities: List<FormEntity> = emptyList(),
    val invalidEntities: List<InvalidEntity> = emptyList()
)
