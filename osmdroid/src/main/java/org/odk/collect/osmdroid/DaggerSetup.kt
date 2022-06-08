package org.odk.collect.osmdroid

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface OsmDroidDependencyComponentProvider {
    val osmDroidDependencyComponent: OsmDroidDependencyComponent
}

@Component(modules = [OsmDroidDependencyModule::class])
@Singleton
interface OsmDroidDependencyComponent {
    fun inject(osmDroidMapFragment: OsmDroidMapFragment)
}

@Module
open class OsmDroidDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesMapConfigurator(): MapConfigurator {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
