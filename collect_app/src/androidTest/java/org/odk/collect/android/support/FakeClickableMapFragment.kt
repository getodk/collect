package org.odk.collect.android.support

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapPoint

class FakeClickableMapFragment : MapFragment {

    private var idCounter = 1
    private var featureClickListener: MapFragment.FeatureListener? = null

    override fun applyConfig(config: Bundle?) { }

    override fun addTo(
        fragmentManager: FragmentManager,
        containerId: Int,
        readyListener: MapFragment.ReadyListener?,
        errorListener: MapFragment.ErrorListener?
    ) {
        readyListener?.onReady(this)
    }

    override fun getCenter(): MapPoint {
        return MapPoint(0.0, 0.0)
    }

    override fun getZoom(): Double {
        return 1.0
    }

    override fun setCenter(center: MapPoint?, animate: Boolean) {}

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {}

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {}

    override fun zoomToBoundingBox(
        points: MutableIterable<MapPoint>?,
        scaleFactor: Double,
        animate: Boolean
    ) {}

    override fun addMarker(point: MapPoint?, draggable: Boolean, iconAnchor: String?): Int {
        val id = idCounter++
        return id
    }

    override fun setMarkerIcon(featureId: Int, drawableId: Int) {}

    override fun getMarkerPoint(featureId: Int): MapPoint {
        return MapPoint(0.0, 0.0)
    }

    override fun addDraggablePoly(points: MutableIterable<MapPoint>, closedPolygon: Boolean): Int {
        return -1
    }

    override fun appendPointToPoly(featureId: Int, point: MapPoint) {}

    override fun removePolyLastPoint(featureId: Int) {}

    override fun getPolyPoints(featureId: Int): MutableList<MapPoint> {
        return mutableListOf()
    }

    override fun removeFeature(featureId: Int) {}

    override fun clearFeatures() {}

    override fun setClickListener(listener: MapFragment.PointListener?) {}

    override fun setLongPressListener(listener: MapFragment.PointListener?) {}

    override fun setFeatureClickListener(listener: MapFragment.FeatureListener?) {
        featureClickListener = listener
    }

    override fun setDragEndListener(listener: MapFragment.FeatureListener?) {}

    override fun setGpsLocationEnabled(enabled: Boolean) {}

    override fun getGpsLocation(): MapPoint? {
        return null
    }

    override fun getLocationProvider(): String? {
        return null
    }

    override fun runOnGpsLocationReady(listener: MapFragment.ReadyListener) {}

    override fun setGpsLocationListener(listener: MapFragment.PointListener?) {}

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {}

    fun clickOnFeature(featureId: Int) {
        var done = false

        Handler(Looper.getMainLooper()).post {
            featureClickListener?.onFeature(featureId)
            done = true
        }

        while (!done) {
            Thread.sleep(1)
        }
    }
}
