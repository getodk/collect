package org.odk.collect.android.injection.config

import android.app.Application
import android.content.Context
import android.location.LocationManager
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.location.LocationClient
import org.odk.collect.location.satellites.GpsStatusSatelliteInfoClient
import org.odk.collect.location.satellites.SatelliteInfoClient
import org.odk.collect.location.tracker.ForegroundServiceLocationTracker
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.ExternalWebPageHelper

class CollectGeoDependencyModule(
    private val appDependencyComponent: AppDependencyComponent
) : GeoDependencyModule() {

    override fun providesMapFragmentFactory(): MapFragmentFactory {
        return appDependencyComponent.mapFragmentFactory()
    }

    override fun providesLocationTracker(application: Application): LocationTracker {
        return ForegroundServiceLocationTracker(application)
    }

    override fun providesLocationClient(): LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesScheduler(): Scheduler {
        return appDependencyComponent.scheduler()
    }

    override fun providesSatelliteInfoClient(context: Context): SatelliteInfoClient {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return GpsStatusSatelliteInfoClient(locationManager)
    }

    override fun providesPermissionChecker(context: Context): PermissionsChecker {
        return appDependencyComponent.permissionsChecker()
    }

    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }

    override fun providesExternalWebPageHelper(): ExternalWebPageHelper {
        return appDependencyComponent.externalWebPageHelper()
    }
}
