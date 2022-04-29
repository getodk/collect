package org.odk.collect.geo.support

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.FeatureListener
import org.odk.collect.maps.MapFragment.PointListener
import org.odk.collect.maps.MapFragment.ReadyListener
import org.odk.collect.maps.MapPoint

class FakeMapFragment : MapFragment {

    private var gpsLocationListener: PointListener? = null
    private var locationProvider: String? = null
    private var retainMockAccuracy = false
    private var center: MapPoint? = null
    private var zoom = 0.0
    private var zoomBoundingBox: Pair<Iterable<MapPoint>, Double>? = null

    private var gpsLocation: MapPoint? = null
    private var featureClickListener: FeatureListener? = null
    private val markers: MutableList<MapPoint> = ArrayList()
    private val markerIcons: MutableList<Int?> = ArrayList()

    override fun applyConfig(config: Bundle) {}

    override fun addTo(
        fragmentManager: FragmentManager,
        containerId: Int,
        readyListener: ReadyListener?,
        errorListener: MapFragment.ErrorListener?
    ) {
        readyListener!!.onReady(this)
    }

    override fun getCenter(): MapPoint {
        return center ?: DEFAULT_CENTER
    }

    override fun getZoom(): Double {
        return zoom
    }

    override fun setCenter(center: MapPoint?, animate: Boolean) {
        this.center = center
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
        this.center = center
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
        zoomBoundingBox = null
        this.center = center
        this.zoom = zoom
    }

    override fun zoomToBoundingBox(
        points: Iterable<MapPoint>,
        scaleFactor: Double,
        animate: Boolean
    ) {
        center = null
        zoom = 0.0
        zoomBoundingBox = Pair(points, scaleFactor)
    }

    override fun addMarker(point: MapPoint, draggable: Boolean, iconAnchor: String, iconDrawableId: Int): Int {
        markers.add(point)
        markerIcons.add(null)
        markerIcons[markers.size - 1] = iconDrawableId
        return markers.size - 1
    }

    override fun setMarkerIcon(featureId: Int, drawableId: Int) {
        markerIcons[featureId] = drawableId
    }

    override fun getMarkerPoint(featureId: Int): MapPoint {
        TODO()
    }

    override fun addDraggablePoly(points: Iterable<MapPoint>, closedPolygon: Boolean): Int {
        return 0
    }

    override fun appendPointToPoly(featureId: Int, point: MapPoint) {}
    override fun removePolyLastPoint(featureId: Int) {}
    override fun getPolyPoints(featureId: Int): List<MapPoint> {
        return emptyList()
    }

    override fun clearFeatures() {
        markers.clear()
        markerIcons.clear()
    }

    override fun setClickListener(listener: PointListener?) {}
    override fun setLongPressListener(listener: PointListener?) {}
    override fun setFeatureClickListener(listener: FeatureListener?) {
        featureClickListener = listener
    }

    override fun setDragEndListener(listener: FeatureListener?) {}
    override fun setGpsLocationEnabled(enabled: Boolean) {}
    override fun getGpsLocation(): MapPoint? {
        return gpsLocation
    }

    override fun getLocationProvider(): String? {
        return locationProvider
    }

    override fun runOnGpsLocationReady(listener: ReadyListener) {}
    override fun setGpsLocationListener(listener: PointListener?) {
        gpsLocationListener = listener

        gpsLocation?.let {
            listener?.onPoint(it)
        }
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        this.retainMockAccuracy = retainMockAccuracy
    }

    fun setLocation(mapPoint: MapPoint?) {
        gpsLocation = mapPoint
        if (gpsLocationListener != null) {
            gpsLocationListener!!.onPoint(mapPoint!!)
        }
    }

    fun setLocationProvider(locationProvider: String?) {
        this.locationProvider = locationProvider
    }

    fun isRetainMockAccuracy(): Boolean {
        return retainMockAccuracy
    }

    fun clickOnFeature(index: Int) {
        featureClickListener!!.onFeature(index)
    }

    fun getMarkers(): List<MapPoint> {
        return markers
    }

    fun getMarkerIcons(): List<Int?> {
        return markerIcons
    }

    fun getZoomBoundingBox(): Pair<Iterable<MapPoint>, Double>? {
        return zoomBoundingBox
    }

    companion object {
        /**
         * The value returned if the map has had no center set or has had `null` pass to
         * [setCenter]
         */
        val DEFAULT_CENTER = MapPoint(-1.0, -1.0)
    }
}
