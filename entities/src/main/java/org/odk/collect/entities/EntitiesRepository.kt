package org.odk.collect.entities

interface EntitiesRepository {
    fun save(vararg entities: Entity)
    fun getLists(): Set<String>
    fun getEntities(list: String): List<Entity>
    fun clear()
    fun addList(list: String)
    fun delete(id: String)
}
