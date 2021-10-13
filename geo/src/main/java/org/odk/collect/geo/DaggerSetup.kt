package org.odk.collect.geo

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.location.tracker.LocationTracker
import java.lang.UnsupportedOperationException
import javax.inject.Singleton

interface GeoDependencyComponentProvider {
    val geoDependencyComponent: GeoDependencyComponent
}

@Component(modules = [GeoDependencyModule::class])
@Singleton
interface GeoDependencyComponent {
    fun inject(geoPointMapActivity: GeoPointMapActivity)
    fun inject(geoPolyActivity: GeoPolyActivity)
}

@Module
open class GeoDependencyModule {

    @Provides
    open fun providesMapFragmentFactory(): MapFragmentFactory {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationTracker(): LocationTracker {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
