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
import org.odk.collect.maps.layers.OfflineMapLayersPickerViewModel
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.webpage.ExternalWebPageHelper

class CollectGeoDependencyModule(
    private val mapFragmentFactory: MapFragmentFactory,
    private val locationClient: LocationClient,
    private val scheduler: Scheduler,
    private val permissionChecker: PermissionsChecker,
    private val offlineMapLayersPickerViewModelFactory: OfflineMapLayersPickerViewModel.Factory,
    private val externalWebPageHelper: ExternalWebPageHelper
) : GeoDependencyModule() {

    override fun providesMapFragmentFactory(): MapFragmentFactory {
        return mapFragmentFactory
    }

    override fun providesLocationTracker(application: Application): LocationTracker {
        return ForegroundServiceLocationTracker(application)
    }

    override fun providesLocationClient(): LocationClient {
        return locationClient
    }

    override fun providesScheduler(): Scheduler {
        return scheduler
    }

    override fun providesSatelliteInfoClient(context: Context): SatelliteInfoClient {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return GpsStatusSatelliteInfoClient(locationManager)
    }

    override fun providesPermissionChecker(context: Context): PermissionsChecker {
        return permissionChecker
    }

    override fun providesOfflineMapLayersPickerViewModelFactory(): OfflineMapLayersPickerViewModel.Factory {
        return offlineMapLayersPickerViewModelFactory
    }

    override fun providesExternalWebPageHelper(): ExternalWebPageHelper {
        return externalWebPageHelper
    }
}
