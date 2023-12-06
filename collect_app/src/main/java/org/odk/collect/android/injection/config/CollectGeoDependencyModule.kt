package org.odk.collect.android.injection.config

import OfflineMapLayer
import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import org.odk.collect.android.formlists.sorting.OfflineMapLayersListBottomSheetDialog
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
import java.util.function.Consumer
import org.odk.collect.android.R

class CollectGeoDependencyModule(
    private val mapFragmentFactory: MapFragmentFactory,
    private val locationClient: LocationClient,
    private val scheduler: Scheduler,
    private val permissionChecker: PermissionsChecker
) : GeoDependencyModule() {

    override fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
        return object : ReferenceLayerSettingsNavigator {
            override fun navigateToReferenceLayerSettings(activity: FragmentActivity) {
                // Create a few OfflineMapLayer items
                val options: List<OfflineMapLayer> = listOf(
                        OfflineMapLayer("Map Layer 1", R.drawable.ic_map),
                        OfflineMapLayer("Map Layer 2", R.drawable.ic_map),
                )
                val selectedOption: Int = 0
                val onSelectedOptionChanged = Consumer<Int> { position ->
                    // Handle the selected option change
                    // For example: Update UI or perform actions based on the selected option
                }
                // Call the showBottomSheet method
                OfflineMapLayersListBottomSheetDialog.showBottomSheet(activity, options, selectedOption, onSelectedOptionChanged)

//                MapsPreferencesFragment.showReferenceLayerDialog(activity)
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
