package org.odk.collect.maps

import android.os.Bundle

object MapFragmentUtils {

    private const val MAP_CENTER_KEY = "map_center"
    private const val MAP_ZOOM_KEY = "map_zoom"

    @JvmStatic
    fun onSaveInstanceState(mapFragment: MapFragment, outState: Bundle) {
        outState.putParcelable(MAP_CENTER_KEY, mapFragment.center)
        outState.putDouble(MAP_ZOOM_KEY, mapFragment.zoom)
    }

    @JvmStatic
    fun onMapReady(mapFragment: MapFragment, savedInstanceState: Bundle?) {
        val mapCenter: MapPoint? = savedInstanceState?.getParcelable(MAP_CENTER_KEY)
        val mapZoom: Double? = savedInstanceState?.getDouble(MAP_ZOOM_KEY)

        if (mapCenter != null && mapZoom != null) {
            mapFragment.zoomToPoint(mapCenter, mapZoom, false)
        } else if (mapCenter != null) {
            mapFragment.zoomToPoint(mapCenter, false)
        }
    }
}
