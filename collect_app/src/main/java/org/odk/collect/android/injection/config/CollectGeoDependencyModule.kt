package org.odk.collect.android.injection.config

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.ReferenceLayerSettingsNavigator
import org.odk.collect.location.LocationClient
import org.odk.collect.location.satellites.GpsStatusSatelliteInfoClient
import org.odk.collect.location.satellites.SatelliteInfoClient
import org.odk.collect.location.tracker.ForegroundServiceLocationTracker
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.permissions.PermissionsChecker

class CollectGeoDependencyModule(
    private val mapFragmentFactory: MapFragmentFactory,
    private val locationClient: LocationClient,
    private val scheduler: Scheduler,
    private val permissionChecker: PermissionsChecker,
) : GeoDependencyModule() {

    override fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
        return object : ReferenceLayerSettingsNavigator {
            override fun navigateToReferenceLayerSettings(activity: FragmentActivity) {
                MapsPreferencesFragment.showReferenceLayerDialog(activity)
            }
        }
    }

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
}
