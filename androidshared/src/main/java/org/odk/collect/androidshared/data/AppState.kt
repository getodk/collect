package org.odk.collect.androidshared.data

import android.app.Application

class AppState {

    private val map = mutableMapOf<String, Any?>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T {
        return map[key] as T
    }

    fun set(key: String, value: Any?) {
        map[key] = value
    }
}

interface StateStore {
    fun getState(): AppState
}

fun Application.getState(): AppState {
    val stateStore = this as StateStore
    return stateStore.getState()
}
