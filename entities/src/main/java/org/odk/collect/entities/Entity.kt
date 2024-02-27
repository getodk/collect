package org.odk.collect.entities

data class Entity(
    val dataset: String,
    val id: String,
    val label: String?,
    val version: Int = 1,
    val properties: List<Pair<String, String>> = emptyList()
)
