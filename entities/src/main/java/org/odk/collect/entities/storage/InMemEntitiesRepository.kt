package org.odk.collect.entities.storage

class InMemEntitiesRepository : EntitiesRepository {

    private val lists = mutableSetOf<String>()
    private val listProperties = mutableMapOf<String, MutableSet<String>>()
    private val listVersions = mutableMapOf<String, String>()
    private val entities = mutableMapOf<String, MutableList<Entity.New>>()

    override fun getLists(): Set<String> {
        return lists
    }

    override fun getEntities(list: String): List<Entity.Saved> {
        val entities = entities[list] ?: emptyList()
        return entities.mapIndexed { index, entity ->
            Entity.Saved(
                entity.id,
                entity.label,
                entity.version,
                buildProperties(list, entity),
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
        entities.forEach { (_, list) ->
            list.removeIf { it.id == id }
        }
    }

    override fun getById(list: String, id: String): Entity.Saved? {
        return getEntities(list).firstOrNull { it.id == id }
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        return if (listProperties[list]?.contains(property) == true) {
            getEntities(list).filter { entity ->
                entity.properties.any { (first, second) -> first == property && second == value }
            }.toList()
        } else if (value == "") {
            getEntities(list)
        } else {
            emptyList()
        }
    }

    override fun getByIndex(list: String, index: Int): Entity.Saved? {
        return getEntities(list).firstOrNull { it.index == index }
    }

    override fun updateListHash(list: String, hash: String) {
        listVersions[list] = hash
    }

    override fun getListHash(list: String): String? {
        return listVersions[list]
    }

    override fun save(list: String, vararg entities: Entity) {
        val entityList = this.entities.getOrPut(list) { mutableListOf() }

        entities.forEach { entity ->
            updateLists(list, entity)
            val existing = entityList.find { it.id == entity.id }

            if (existing != null) {
                val state = when (existing.state) {
                    Entity.State.OFFLINE -> entity.state
                    Entity.State.ONLINE -> Entity.State.ONLINE
                }

                entityList.remove(existing)
                entityList.add(
                    Entity.New(
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
                entityList.add(
                    Entity.New(
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

    private fun updateLists(list: String, entity: Entity) {
        lists.add(list)
        listProperties.getOrPut(list) {
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

    private fun buildProperties(list: String, entity: Entity.New): List<Pair<String, String>> {
        return listProperties[list]?.map { property ->
            Pair(
                property,
                entity.properties.find { it.first == property }?.second ?: ""
            )
        } ?: emptyList()
    }
}
