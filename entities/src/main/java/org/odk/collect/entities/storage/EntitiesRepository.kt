package org.odk.collect.entities.storage

import org.odk.collect.shared.Query

interface EntitiesRepository {
    fun save(list: String, vararg entities: Entity)
    fun getLists(): Set<String>

    @Deprecated(
        message = "Should use #query instead",
        ReplaceWith(
            "query(list, null)"
        )
    )
    fun getEntities(list: String): List<Entity.Saved>
    fun getCount(list: String): Int
    fun clear()
    fun addList(list: String)
    fun delete(id: String)
    fun query(list: String, query: Query): List<Entity.Saved>

    @Deprecated(
        message = "Should use #query instead",
        ReplaceWith(
            "query(list, Query.Eq(EntitiesTable.COLUMN_ID, id))"
        )
    )
    fun getById(list: String, id: String): Entity.Saved?
    fun getAllByProperty(list: String, property: String, value: String): List<Entity.Saved>

    @Deprecated(
        message = "Should use #query instead",
        ReplaceWith(
            "query(list, Query.Eq(\"i.\$ROW_ID\", (index + 1).toString()))"
        )
    )
    fun getByIndex(list: String, index: Int): Entity.Saved?
    fun updateListHash(list: String, hash: String)
    fun getListHash(list: String): String?
}
