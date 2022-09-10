package org.odk.collect.android.widgets.support

import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

class NoOpMapFragment : Fragment(), MapFragment {

    override fun init(
        readyListener: MapFragment.ReadyListener?,
        errorListener: MapFragment.ErrorListener?,
    ) {
    }

    override fun getCenter(): MapPoint {
        TODO("Not yet implemented")
    }

    override fun getZoom(): Double {
        TODO("Not yet implemented")
    }

    override fun setCenter(center: MapPoint?, animate: Boolean) {
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
    }

    override fun zoomToBoundingBox(
        points: MutableIterable<MapPoint>?,
        scaleFactor: Double,
        animate: Boolean,
    ) {
    }

    override fun addMarker(markerDescription: MarkerDescription): Int {
        TODO("Not yet implemented")
    }

    override fun addMarkers(markers: MutableList<MarkerDescription>?): MutableList<Int> {
        TODO("Not yet implemented")
    }

    override fun setMarkerIcon(featureId: Int, markerIconDescription: MarkerIconDescription) {
    }

    override fun getMarkerPoint(featureId: Int): MapPoint {
        TODO("Not yet implemented")
    }

    override fun addDraggablePoly(points: MutableIterable<MapPoint>, closedPolygon: Boolean): Int {
        TODO("Not yet implemented")
    }

    override fun appendPointToPoly(featureId: Int, point: MapPoint) {
    }

    override fun removePolyLastPoint(featureId: Int) {
    }

    override fun getPolyPoints(featureId: Int): MutableList<MapPoint> {
        TODO("Not yet implemented")
    }

    override fun clearFeatures() {
    }

    override fun setClickListener(listener: MapFragment.PointListener?) {
    }

    override fun setLongPressListener(listener: MapFragment.PointListener?) {
    }

    override fun setFeatureClickListener(listener: MapFragment.FeatureListener?) {
    }

    override fun setDragEndListener(listener: MapFragment.FeatureListener?) {
    }

    override fun setGpsLocationEnabled(enabled: Boolean) {
    }

    override fun getGpsLocation(): MapPoint? {
        TODO("Not yet implemented")
    }

    override fun getLocationProvider(): String? {
        TODO("Not yet implemented")
    }

    override fun runOnGpsLocationReady(listener: MapFragment.ReadyListener) {
    }

    override fun setGpsLocationListener(listener: MapFragment.PointListener?) {
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
    }

    override fun hasCenter(): Boolean {
        return false
    }
}
