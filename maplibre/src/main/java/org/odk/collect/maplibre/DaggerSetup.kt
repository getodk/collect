package org.odk.collect.maplibre

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface MapLibreDependencyComponentProvider {
    val mapLibreDependencyComponent: MapLibreDependencyComponent
}

@Component(modules = [MapLibreDependencyModule::class])
@Singleton
interface MapLibreDependencyComponent {
    fun inject(mapLibreMapFragment: MapLibreMapFragment)
}

@Module
open class MapLibreDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
