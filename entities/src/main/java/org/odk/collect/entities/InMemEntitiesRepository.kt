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
        val existing = entities.find { it.id == entity.id && it.dataset == entity.dataset }

        if (existing != null) {
            entities.remove(existing)
            entities.add(
                Entity(
                    entity.dataset,
                    entity.id,
                    entity.label,
                    mergeProperties(existing, entity)
                )
            )
        } else {
            entities.add(entity)
        }
    }

    private fun mergeProperties(
        existing: Entity,
        new: Entity
    ): List<Pair<String, String>> {
        val existingProperties = mutableMapOf(*existing.properties.toTypedArray())
        new.properties.forEach {
            existingProperties[it.first] = it.second
        }

        return existingProperties.map { Pair(it.key, it.value) }
    }
}
