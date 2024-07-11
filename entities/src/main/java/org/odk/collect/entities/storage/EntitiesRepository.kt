package org.odk.collect.entities.storage

interface EntitiesRepository {
    fun save(vararg entities: Entity)
    fun getLists(): Set<String>
    fun getEntities(list: String): List<Entity.Saved>
    fun clear()
    fun addList(list: String)
    fun delete(id: String)
    fun getById(list: String, id: String): Entity.Saved?
}
