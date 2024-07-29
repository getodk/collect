package org.odk.collect.entities.storage

class InMemEntitiesRepository : EntitiesRepository {

    private val lists = mutableSetOf<String>()
    private val entities = mutableListOf<Entity.New>()

    override fun getLists(): Set<String> {
        return lists
    }

    override fun getEntities(list: String): List<Entity.Saved> {
        return entities.filter { it.list == list }.mapIndexed { index, entity ->
            Entity.Saved(
                entity.list,
                entity.id,
                entity.label,
                entity.version,
                entity.properties,
                entity.state,
                index
            )
        }
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

    override fun getById(list: String, id: String): Entity.Saved? {
        return getEntities(list).firstOrNull { it.id == id }
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        return getEntities(list).filter { entity ->
            entity.properties.any { (first, second) -> first == property && second == value }
        }
    }

    override fun save(vararg entities: Entity) {
        entities.forEach { entity ->
            lists.add(entity.list)
            val existing = this.entities.find { it.id == entity.id && it.list == entity.list }

            if (existing != null) {
                val state = when (existing.state) {
                    Entity.State.OFFLINE -> entity.state
                    Entity.State.ONLINE -> Entity.State.ONLINE
                }

                this.entities.remove(existing)
                this.entities.add(
                    Entity.New(
                        entity.list,
                        entity.id,
                        entity.label ?: existing.label,
                        version = entity.version,
                        properties = mergeProperties(existing, entity),
                        state = state
                    )
                )
            } else {
                this.entities.add(
                    Entity.New(
                        entity.list,
                        entity.id,
                        entity.label,
                        entity.version,
                        entity.properties,
                        entity.state
                    )
                )
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
