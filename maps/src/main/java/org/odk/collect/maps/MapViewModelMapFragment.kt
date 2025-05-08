package org.odk.collect.maps

import androidx.fragment.app.Fragment

/**
 * Convenience abstract class for implementing a [MapFragment] using [MapViewModel] to handle
 * common behaviour and state.
 */
abstract class MapViewModelMapFragment : Fragment(), MapFragment {

    abstract fun getMapViewModel(): MapViewModel

    final override fun setCenter(center: MapPoint?, animate: Boolean) {
        getMapViewModel().moveTo(center, animate)
    }

    final override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
        getMapViewModel().zoomTo(center, null, animate)
    }

    final override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
        getMapViewModel().zoomTo(center, zoom, animate)
    }

    final override fun zoomToBoundingBox(
        points: Iterable<MapPoint>,
        scaleFactor: Double,
        animate: Boolean
    ) {
        getMapViewModel().zoomTo(points.toList(), scaleFactor, animate)
    }

    final override fun zoomToCurrentLocation(center: MapPoint?) {
        getMapViewModel().zoomToCurrentLocation(center)
    }

    final override fun hasCenter(): Boolean {
        return getMapViewModel().zoom.getValue() != null
    }
}
