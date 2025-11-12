package org.odk.collect.location

import android.app.Application
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.StateStore
import org.odk.collect.androidshared.utils.InMemUniqueIdGenerator
import org.odk.collect.androidshared.utils.UniqueIdGenerator

class RobolectricApplication : Application(), StateStore, LocationDependencyComponentProvider {

    override val locationDependencyComponent: LocationDependencyComponent =
        DaggerLocationDependencyComponent.builder().locationDependencyModule(
            object : LocationDependencyModule() {
                override fun providesUniqueIdGenerator(): UniqueIdGenerator {
                    return InMemUniqueIdGenerator()
                }
            }
        ).build()

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}
