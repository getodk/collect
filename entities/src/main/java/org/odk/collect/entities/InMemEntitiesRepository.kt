package org.odk.collect.entities

class InMemEntitiesRepository : EntitiesRepository {

    private val entities = mutableListOf<Entity>()

    override fun getDatasets(): Set<String> {
        return entities.map { it.dataset }.toSet()
    }

    override fun getEntities(dataset: String): List<Entity> {
        return entities.filter { it.dataset == dataset }
    }

    override fun save(entity: Entity) {
        entities.add(entity)
    }
}
