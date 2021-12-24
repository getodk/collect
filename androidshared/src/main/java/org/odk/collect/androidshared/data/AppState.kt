package org.odk.collect.androidshared.data

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

/**
 * [AppState] can be used as a shared store of state that lives at an "app"/"in-memory" level
 * rather than being tied to a specific component. This could be shared state between different
 * [Activity] objects or a way of communicating between a [Service] and other components.
 * [AppState] can be used as an alternative to Dagger singleton objects or static fields.
 *
 * [AppState] should not be used to share state between views or components on the same screen or make
 * up part of the same flow. For this, using Jetpack's [ViewModel] at either a [Fragment] or [Activity]
 * level is more appropriate.
 *
 * The easiest way to use [AppState] is have an instance owned by your app's [Application] object
 * and implement the [StateStore] interface:
 *
 * ```
 * class MyApplication : Application(), StateStore {
 *     private val appState = AppState()
 * }
 * ```
 *
 * The [AppState] instance can then be accessed anywhere the [Application] is available using the
 * [getState] extension function.
 *
 */
class AppState {

    private val map = mutableMapOf<String, Any?>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T? = null): T {
        return map.getOrPut(key) { default } as T
    }

    fun set(key: String, value: Any?) {
        map[key] = value
    }

    fun clear() {
        map.clear()
    }

    fun clear(key: String) {
        map.remove(key)
    }
}

interface StateStore {
    fun getState(): AppState
}

fun Application.getState(): AppState {
    val stateStore = this as StateStore
    return stateStore.getState()
}
