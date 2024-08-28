package org.odk.collect.entities.storage

sealed interface Entity {
    val id: String
    val label: String?
    val version: Int
    val properties: List<Pair<String, String>>
    val state: State
    val trunkVersion: Int?
    val branchId: String

    data class New(
        override val id: String,
        override val label: String?,
        override val version: Int = 1,
        override val properties: List<Pair<String, String>> = emptyList(),
        override val state: State = State.OFFLINE,
        override val trunkVersion: Int? = null,
        override val branchId: String = ""
    ) : Entity

    data class Saved(
        override val id: String,
        override val label: String?,
        override val version: Int = 1,
        override val properties: List<Pair<String, String>> = emptyList(),
        override val state: State = State.OFFLINE,
        val index: Int,
        override val trunkVersion: Int? = null,
        override val branchId: String = ""
    ) : Entity

    enum class State {
        OFFLINE,
        ONLINE
    }

    fun sameAs(entity: Entity): Boolean {
        val a = convertToNew(this)
        val b = convertToNew(entity)
        return a == b
    }

    private fun convertToNew(entity: Entity): New {
        return New(
            entity.id,
            entity.label,
            entity.version,
            entity.properties,
            entity.state,
            entity.trunkVersion,
            entity.branchId
        )
    }
}
