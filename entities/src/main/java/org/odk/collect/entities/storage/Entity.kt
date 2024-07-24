package org.odk.collect.entities.storage

import java.util.UUID

sealed interface Entity {
    val list: String
    val id: String
    val label: String?
    val version: Int
    val properties: List<Pair<String, String>>
    val state: State
    val trunkVersion: Int?
    val branchId: String

    data class New(
        override val list: String,
        override val id: String,
        override val label: String?,
        override val version: Int = 1,
        override val properties: List<Pair<String, String>> = emptyList(),
        override val state: State = State.OFFLINE,
        override val trunkVersion: Int? = null,
        override val branchId: String = UUID.randomUUID().toString()
    ) : Entity

    data class Saved(
        override val list: String,
        override val id: String,
        override val label: String?,
        override val version: Int = 1,
        override val properties: List<Pair<String, String>> = emptyList(),
        override val state: State = State.OFFLINE,
        val index: Int,
        override val trunkVersion: Int? = null,
        override val branchId: String = UUID.randomUUID().toString()
    ) : Entity

    enum class State(val id: Int) {
        OFFLINE(0),
        ONLINE(1);

        companion object {
            fun fromId(id: Int): State {
                return entries.first { it.id == id }
            }
        }
    }

    fun sameAs(entity: Entity): Boolean {
        val a = convertToNew(this)
        val b = convertToNew(entity)
        return a == b
    }

    private fun convertToNew(entity: Entity): New {
        return New(
            entity.list,
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
