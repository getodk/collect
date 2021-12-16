package org.odk.collect.geo

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.location.LocationClient
import org.odk.collect.location.tracker.LocationTracker
import javax.inject.Singleton

interface GeoDependencyComponentProvider {
    val geoDependencyComponent: GeoDependencyComponent
}

@Component(modules = [GeoDependencyModule::class])
@Singleton
interface GeoDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun geoDependencyModule(geoDependencyModule: GeoDependencyModule): Builder

        fun build(): GeoDependencyComponent
    }

    fun inject(geoPointActivity: GeoPointActivity)
    fun inject(geoPointMapActivity: GeoPointMapActivity)
    fun inject(geoPolyActivity: GeoPolyActivity)
    fun inject(geoPointDialogFragment: GeoPointDialogFragment)
    fun inject(geoPointActivityNew: GeoPointActivityNew)

    val scheduler: Scheduler
    val locationTracker: LocationTracker
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

    @Provides
    open fun providesLocationClient(application: Application): LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesScheduler(): Scheduler {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    internal open fun providesGeoPointViewModelFactory(application: Application): GeoPointViewModelFactory {
        return object : GeoPointViewModelFactory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val componentProvider = application as GeoDependencyComponentProvider
                val component = componentProvider.geoDependencyComponent
                return GeoPointViewModelImpl(component.locationTracker, component.scheduler) as T
            }
        }
    }
}
