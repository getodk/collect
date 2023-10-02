package org.odk.collect.googlemaps

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface GoogleMapsDependencyComponentProvider {
    val googleMapsDependencyComponent: GoogleMapsDependencyComponent
}

@Component(modules = [GoogleMapsDependencyModule::class])
@Singleton
interface GoogleMapsDependencyComponent {
    fun inject(osmDroidMapFragment: GoogleMapFragment)
}

@Module
open class GoogleMapsDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
