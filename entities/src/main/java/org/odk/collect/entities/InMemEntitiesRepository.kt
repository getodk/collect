package org.odk.collect.entities

class InMemEntitiesRepository : EntitiesRepository {

    private val lists = mutableSetOf<String>()
    private val entities = mutableListOf<Entity>()

    override fun getLists(): Set<String> {
        return lists
    }

    override fun getEntities(list: String): List<Entity> {
        return entities.filter { it.list == list }
    }

    override fun clear() {
        entities.clear()
        lists.clear()
    }

    override fun addList(list: String) {
        lists.add(list)
    }

    override fun delete(id: String) {
        entities.removeIf { it.id == id }
    }

    override fun save(vararg entities: Entity) {
        entities.forEach { entity ->
            lists.add(entity.list)
            val existing = this.entities.find { it.id == entity.id }

            if (existing != null) {
                val state = when (existing.state) {
                    Entity.State.OFFLINE -> entity.state
                    Entity.State.ONLINE -> Entity.State.ONLINE
                }

                this.entities.remove(existing)
                this.entities.add(
                    Entity(
                        entity.list,
                        entity.id,
                        entity.label ?: existing.label,
                        version = entity.version,
                        properties = mergeProperties(existing, entity),
                        state = state
                    )
                )
            } else {
                this.entities.add(entity)
            }
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
