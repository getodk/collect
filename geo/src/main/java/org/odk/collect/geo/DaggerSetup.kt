package org.odk.collect.geo

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.geo.maps.MapFragmentFactory
import javax.inject.Singleton

interface GeoDependencyComponentProvider {
    val geoDependencyComponent: GeoDependencyComponent
}

@Component(modules = [GeoDependencyModule::class])
@Singleton
interface GeoDependencyComponent {
    fun inject(geoPointMapActivity: GeoPointMapActivity)
}

@Module
open class GeoDependencyModule {

    @Provides
    open fun providesMapFragmentFactory(): MapFragmentFactory {
        TODO()
    }

    @Provides
    open fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
        TODO()
    }
}
