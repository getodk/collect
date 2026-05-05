package org.odk.collect.entities.storage

import org.odk.collect.shared.Query

interface EntitiesRepository {
    /**
     * Saves an [Entity]. Properties ([Entity.properties]) will be dynamically added to the [list]
     * if they don't already exist - [Entity] instances returned by follow-up calls to [query] will
     * include them.
     *
     * To remove properties that shouldn't be in the [list] any longer, see [cleanUpProperties].
     */
    fun save(list: String, vararg entities: Entity)
    fun getLists(): List<EntityList>
    fun getCount(list: String): Int
    fun addList(list: String)
    fun delete(list: String, id: String)
    fun query(list: String, query: Query? = null): List<Entity.Saved>
    fun getByIndex(list: String, index: Int): Entity.Saved?
    fun updateList(list: String, hash: String, needsApproval: Boolean)
    fun getList(list: String): EntityList?

    fun cleanUpProperties(list: String, properties: Set<String>)
}

fun EntitiesRepository.getListNames(): List<String> {
    return getLists().map { it.name }
}
