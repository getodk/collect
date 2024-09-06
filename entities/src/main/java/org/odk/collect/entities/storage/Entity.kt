package org.odk.collect.entities.storage

sealed interface Entity {
    val id: String
    val label: String?
    val version: Int
    val properties: List<Pair<String, String>>
    val state: State

    /**
     * The server version (from an entity list CSV) this is based on. This should only be updated
     * when updating an entity from the server where as [version] should be incremented whenever
     * there is a local change.
     */
    val trunkVersion: Int?

    /**
     * The offline "branch" identifier. Should be updated whenever the local version is modified
     * from the latest server version.
     */
    val branchId: String

    fun isDirty(): Boolean {
        return version != trunkVersion
    }

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
