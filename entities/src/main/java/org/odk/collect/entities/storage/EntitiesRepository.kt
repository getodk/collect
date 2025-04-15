package org.odk.collect.entities.storage

import org.odk.collect.shared.Query

interface EntitiesRepository {
    fun save(list: String, vararg entities: Entity)
    fun getLists(): List<EntityList>
    fun getCount(list: String): Int
    fun addList(list: String)
    fun delete(list: String, id: String)
    fun query(list: String, query: Query? = null): List<Entity.Saved>
    fun getByIndex(list: String, index: Int): Entity.Saved?
    fun updateList(list: String, hash: String, needsApproval: Boolean)
    fun getList(list: String): EntityList?
}

fun EntitiesRepository.getListNames(): List<String> {
    return getLists().map { it.name }
}
