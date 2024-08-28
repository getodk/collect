package org.odk.collect.entities.storage

interface EntitiesRepository {
    fun save(vararg entities: Entity)
    fun getLists(): Set<String>
    fun getEntities(list: String): List<Entity.Saved>
    fun getCount(list: String): Int
    fun clear()
    fun addList(list: String)
    fun delete(id: String)
    fun getById(list: String, id: String): Entity.Saved?
    fun getAllByProperty(list: String, property: String, value: String): List<Entity.Saved>
    fun getByIndex(list: String, index: Int): Entity.Saved?
    fun updateListVersion(list: String, version: String)
    fun getListVersion(list: String): String?
}
