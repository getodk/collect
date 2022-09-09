package org.odk.collect.entities

interface EntitiesRepository {
    fun save(entity: Entity)
    fun getDatasets(): Set<String>
    fun getEntities(dataset: String): List<Entity>
}
