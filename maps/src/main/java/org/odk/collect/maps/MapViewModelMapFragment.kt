package org.odk.collect.maps

import androidx.fragment.app.Fragment
import java.util.function.Consumer

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
        points: Iterable<MapPoint>?,
        scaleFactor: Double,
        animate: Boolean
    ) {
        val box = ArrayList<MapPoint>()
        points!!.forEach(Consumer { e: MapPoint -> box.add(e) })
        getMapViewModel().zoomTo(box, scaleFactor, animate)
    }

    final override fun zoomToCurrentLocation(center: MapPoint?) {
        getMapViewModel().zoomToCurrentLocation(center)
    }

    final override fun hasCenter(): Boolean {
        return getMapViewModel().zoom.getValue() != null
    }

    final override fun getMapFragmentDelegate(): MapFragmentDelegate =
        throw UnsupportedOperationException()
}
