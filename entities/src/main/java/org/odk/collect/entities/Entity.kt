package org.odk.collect.entities

data class Entity @JvmOverloads constructor(
    val list: String,
    val id: String,
    val label: String?,
    val version: Int = 1,
    val properties: List<Pair<String, String>> = emptyList(),
    val state: State = State.OFFLINE
) {
    enum class State {
        OFFLINE,
        ONLINE
    }
}
