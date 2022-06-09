package org.odk.collect.mapbox

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface MapboxDependencyComponentProvider {
    val mapboxDependencyComponent: MapboxDependencyComponent
}

@Component(modules = [MapboxDependencyModule::class])
@Singleton
interface MapboxDependencyComponent {
    fun inject(mapboxMapFragment: MapboxMapFragment)

    fun inject(mapBoxInitializationFragment: MapBoxInitializationFragment)
}

@Module
open class MapboxDependencyModule {
    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesNetworkStateProvider(): NetworkStateProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
