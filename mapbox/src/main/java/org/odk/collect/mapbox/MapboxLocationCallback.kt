package org.odk.collect.mapbox

import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.toAndroidLocation
import org.odk.collect.location.LocationUtils.sanitizeAccuracy
import java.lang.ref.WeakReference

// https://docs.mapbox.com/android/core/guides/#requesting-location-updates
// Replace mock location accuracy with 0 as in LocationClient implementations since Mapbox uses its own location engine.
class MapboxLocationCallback(map: MapboxMapFragment) : LocationObserver {
    private val mapRef: WeakReference<MapboxMapFragment> = WeakReference(map)
    private var retainMockAccuracy = false

    override fun onLocationUpdateReceived(locations: MutableList<Location>) {
        val map = mapRef.get()
        val location = locations.last()
        if (map != null) {
            sanitizeAccuracy(location.toAndroidLocation(), retainMockAccuracy)?.let {
                map.onLocationChanged(it)
            }
        }
    }

    fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        this.retainMockAccuracy = retainMockAccuracy
    }
}
