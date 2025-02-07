package org.odk.collect.androidshared.data

import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.androidshared.data.Updatable.Data
import org.odk.collect.androidshared.data.Updatable.QualifiedData
import kotlin.reflect.KProperty

sealed interface Updatable<T> {
    class QualifiedData<T>(
        private val appState: AppState,
        private val key: String,
        private val default: T
    ) : Updatable<T> {
        fun flow(qualifier: String): StateFlow<T> {
            return appState.getFlow("$qualifier:$key", default)
        }

        fun set(qualifier: String?, value: T) {
            appState.setFlow("$qualifier:$key", value)
        }
    }

    class Data<T>(private val appState: AppState, private val key: String, private val default: T) :
        Updatable<T> {
        fun flow(): StateFlow<T> {
            return appState.getFlow(key, default)
        }

        fun set(value: T) {
            appState.setFlow(key, value)
        }
    }
}

abstract class DataService(
    private val appState: AppState,
    private val onUpdate: (() -> Unit)? = null
) {

    private val updaters = mutableListOf<Updater<*>>()

    fun update(qualifier: String? = null) {
        updaters.forEach { it.update(qualifier) }
        onUpdate?.invoke()
    }

    protected fun <T> data(key: String, default: T): DataDelegate<T> {
        val data = Data(appState, key, default)
        return DataDelegate(data)
    }

    protected fun <T> data(key: String, default: T, updater: () -> T): DataDelegate<T> {
        val data = Data(appState, key, default)
        updaters.add(Updater(data) { updater() })
        return DataDelegate(data)
    }

    protected fun <T> qualifiedData(
        key: String,
        default: T
    ): QualifiedDataDelegate<T> {
        val data = QualifiedData(appState, key, default)
        return QualifiedDataDelegate(data)
    }

    protected fun <T> qualifiedData(
        key: String,
        default: T,
        updater: (String) -> T
    ): QualifiedDataDelegate<T> {
        val data = QualifiedData(appState, key, default)
        updaters.add(Updater(data) { it: String? -> updater(it!!) })
        return QualifiedDataDelegate(data)
    }

    class QualifiedDataDelegate<T>(private val data: QualifiedData<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): QualifiedData<T> {
            return data
        }
    }

    class DataDelegate<T>(private val data: Data<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Data<T> {
            return data
        }
    }

    private class Updater<T>(
        private val updatable: Updatable<T>,
        private val updater: (String?) -> T
    ) {
        fun update(qualifier: String? = null) {
            when (updatable) {
                is Data -> updatable.set(updater(qualifier))
                is QualifiedData -> updatable.set(qualifier, updater(qualifier))
            }
        }
    }
}
