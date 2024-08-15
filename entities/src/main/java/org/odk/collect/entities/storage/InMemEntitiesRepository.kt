package org.odk.collect.entities.storage

class InMemEntitiesRepository : EntitiesRepository {

    private val lists = mutableSetOf<String>()
    private val listProperties = mutableMapOf<String, MutableSet<String>>()
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
                buildProperties(entity),
                entity.state,
                index,
                entity.trunkVersion,
                entity.branchId
            )
        }
    }

    override fun getCount(list: String): Int {
        return getEntities(list).count()
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
        }.toList()
    }

    override fun getByIndex(list: String, index: Int): Entity.Saved? {
        return getEntities(list).firstOrNull { it.index == index }
    }

    override fun save(vararg entities: Entity) {
        entities.forEach { entity ->
            updateLists(entity)
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
                        state = state,
                        trunkVersion = entity.trunkVersion,
                        branchId = entity.branchId
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
                        entity.state,
                        entity.trunkVersion,
                        entity.branchId
                    )
                )
            }
        }
    }

    private fun updateLists(entity: Entity) {
        lists.add(entity.list)
        listProperties.getOrPut(entity.list) {
            mutableSetOf()
        }.addAll(entity.properties.map { it.first })
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

    private fun buildProperties(entity: Entity.New): List<Pair<String, String>> {
        return listProperties[entity.list]?.map { property ->
            Pair(
                property,
                entity.properties.find { it.first == property }?.second ?: ""
            )
        } ?: emptyList()
    }
}
