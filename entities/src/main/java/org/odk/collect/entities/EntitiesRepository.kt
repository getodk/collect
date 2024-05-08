package org.odk.collect.entities

interface EntitiesRepository {
    fun save(vararg entities: Entity)
    fun getDatasets(): Set<String>
    fun getEntities(dataset: String): List<Entity>
    fun clear()
    fun addDataset(dataset: String)
    fun delete(id: String)
}
