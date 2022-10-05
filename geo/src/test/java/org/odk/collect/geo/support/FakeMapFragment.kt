package org.odk.collect.geo.support

import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.FeatureListener
import org.odk.collect.maps.MapFragment.PointListener
import org.odk.collect.maps.MapFragment.ReadyListener
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

class FakeMapFragment : Fragment(), MapFragment {

    private var clickListener: PointListener? = null
    private var gpsLocationListener: PointListener? = null
    private var locationProvider: String? = null
    private var retainMockAccuracy = false
    private var center: MapPoint? = null
    private var zoom = 0.0
    private var zoomBoundingBox: Pair<Iterable<MapPoint>, Double>? = null
    private var readyListener: ReadyListener? = null
    private var gpsLocation: MapPoint? = null
    private var featureClickListener: FeatureListener? = null
    private val markers: MutableList<MapPoint> = ArrayList()
    private val markerIcons: MutableList<MarkerIconDescription?> = ArrayList()
    private var hasCenter = false
    private val polyPoints = mutableMapOf<Int, MutableList<MapPoint>>()

    override fun init(
        readyListener: ReadyListener?,
        errorListener: MapFragment.ErrorListener?,
    ) {
        this.readyListener = readyListener
    }

    fun ready() {
        readyListener?.onReady(this)
    }

    override fun getCenter(): MapPoint {
        return center ?: DEFAULT_CENTER
    }

    override fun getZoom(): Double {
        return zoom
    }

    override fun setCenter(center: MapPoint?, animate: Boolean) {
        this.center = center
        hasCenter = true
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
        zoomBoundingBox = null
        this.center = center
        this.zoom = DEFAULT_POINT_ZOOM
        hasCenter = true
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
        zoomBoundingBox = null
        this.center = center
        this.zoom = zoom
        hasCenter = true
    }

    override fun zoomToBoundingBox(
        points: Iterable<MapPoint>,
        scaleFactor: Double,
        animate: Boolean,
    ) {
        center = null
        zoom = 0.0
        zoomBoundingBox = Pair(
            points.toList(), // Clone list to prevent original changing captured values
            scaleFactor
        )
        hasCenter = true
    }

    override fun addMarker(markerDescription: MarkerDescription): Int {
        markers.add(markerDescription.point)
        markerIcons.add(markerDescription.iconDescription)
        return markers.size - 1
    }

    override fun addMarkers(markers: List<MarkerDescription>): List<Int> {
        return markers.map {
            addMarker(it)
        }
    }

    override fun setMarkerIcon(featureId: Int, markerIconDescription: MarkerIconDescription) {
        markerIcons[featureId] = markerIconDescription
    }

    override fun getMarkerPoint(featureId: Int): MapPoint {
        TODO()
    }

    override fun addDraggablePoly(points: Iterable<MapPoint>, closedPolygon: Boolean): Int {
        return 0
    }

    override fun appendPointToPoly(featureId: Int, point: MapPoint) {
        polyPoints.getOrPut(featureId) { mutableListOf() }.add(point)
    }

    override fun removePolyLastPoint(featureId: Int) {
        polyPoints.getOrPut(featureId) { mutableListOf() }.removeLast()
    }

    override fun getPolyPoints(featureId: Int): List<MapPoint> {
        return polyPoints.getOrPut(featureId) { mutableListOf() }
    }

    override fun clearFeatures() {
        markers.clear()
        markerIcons.clear()
    }

    override fun setClickListener(listener: PointListener?) {
        this.clickListener = listener
    }

    fun click(point: MapPoint) {
        clickListener?.onPoint(point)
    }

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

    override fun hasCenter(): Boolean {
        return hasCenter
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

    fun getMarkerIcons(): List<MarkerIconDescription?> {
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

        /**
         * The value used to zoom when [zoomToPoint] is called without a zoom level
         */
        const val DEFAULT_POINT_ZOOM = -1.0
    }
}
