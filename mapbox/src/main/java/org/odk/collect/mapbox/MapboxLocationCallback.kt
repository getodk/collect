package org.odk.collect.mapbox

import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import org.odk.collect.location.LocationUtils.sanitizeAccuracy
import java.lang.Exception
import java.lang.ref.WeakReference

// https://docs.mapbox.com/android/core/guides/#requesting-location-updates
// Replace mock location accuracy with 0 as in LocationClient implementations since Mapbox uses its own location engine.
class MapboxLocationCallback(map: MapboxMapFragment) : LocationEngineCallback<LocationEngineResult> {
    private val mapRef: WeakReference<MapboxMapFragment> = WeakReference(map)
    private var retainMockAccuracy = false

    override fun onSuccess(result: LocationEngineResult) {
        val map = mapRef.get()
        val location = result.lastLocation
        if (map != null && location != null) {
            sanitizeAccuracy(location, retainMockAccuracy)?.let {
                map.onLocationChanged(it)
            }
        }
    }

    override fun onFailure(exception: Exception) = Unit

    fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        this.retainMockAccuracy = retainMockAccuracy
    }
}
