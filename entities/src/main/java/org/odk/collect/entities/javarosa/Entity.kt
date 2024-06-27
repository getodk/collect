package org.odk.collect.entities.javarosa

class Entity(
    val action: EntityAction,
    val dataset: String,
    val id: String?,
    val label: String?,
    val version: Int,
    val properties: List<Pair<String, String>>
)
