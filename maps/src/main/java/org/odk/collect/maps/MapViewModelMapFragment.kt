package org.odk.collect.maps

import androidx.fragment.app.Fragment
import java.util.function.Consumer

abstract class MapViewModelMapFragment : Fragment(), MapFragment {

    abstract fun getMapViewModel(): MapViewModel

    override fun setCenter(center: MapPoint?, animate: Boolean) {
        getMapViewModel().moveTo(center, animate)
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
        getMapViewModel().zoomTo(center, null, animate)
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
        getMapViewModel().zoomTo(center, zoom, animate)
    }

    override fun zoomToBoundingBox(
        points: Iterable<MapPoint>?,
        scaleFactor: Double,
        animate: Boolean
    ) {
        val box = ArrayList<MapPoint>()
        points!!.forEach(Consumer { e: MapPoint -> box.add(e) })
        getMapViewModel().zoomTo(box, scaleFactor, animate)
    }

    override fun zoomToCurrentLocation(center: MapPoint?) {
        getMapViewModel().zoomToCurrentLocation(center)
    }

    override fun hasCenter(): Boolean {
        return getMapViewModel().zoom.getValue() != null
    }
}
