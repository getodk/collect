package org.odk.collect.android.widgets.support

import androidx.fragment.app.Fragment
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.circles.CircleDescription
import org.odk.collect.maps.traces.PolygonDescription
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

class NoOpMapFragment : Fragment(), MapFragment {

    override fun init(
        readyListener: MapFragment.ReadyListener?,
        errorListener: MapFragment.ErrorListener?
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

    override fun zoomToCurrentLocation(center: MapPoint?) {
    }

    override fun zoomToPoint(center: MapPoint?, animate: Boolean) {
    }

    override fun zoomToPoint(center: MapPoint?, zoom: Double, animate: Boolean) {
    }

    override fun zoomToBoundingBox(
        points: Iterable<MapPoint>,
        scaleFactor: Double,
        animate: Boolean
    ) {
    }

    override fun addMarker(markerDescription: MarkerDescription): Int {
        TODO("Not yet implemented")
    }

    override fun updateMarker(
        featureId: Int,
        markerDescription: MarkerDescription
    ) {
        TODO("Not yet implemented")
    }

    override fun addMarkers(markers: List<MarkerDescription>): MutableList<Int> {
        TODO("Not yet implemented")
    }

    override fun setMarkerIcon(featureId: Int, markerIconDescription: MarkerIconDescription) {
    }

    override fun getMarkerPoint(featureId: Int): MapPoint {
        TODO("Not yet implemented")
    }

    override fun addPolyLine(lineDescription: LineDescription): Int {
        TODO("Not yet implemented")
    }

    override fun updatePolyLine(
        featureId: Int,
        lineDescription: LineDescription
    ) {
        TODO("Not yet implemented")
    }

    override fun addPolygon(polygonDescription: PolygonDescription): Int {
        TODO("Not yet implemented")
    }

    override fun updatePolygon(
        featureId: Int,
        polygonDescription: PolygonDescription
    ) {
        TODO("Not yet implemented")
    }

    override fun addCircle(circleDescription: CircleDescription): Int {
        TODO("Not yet implemented")
    }

    override fun updateCircle(
        featureId: Int,
        circleDescription: CircleDescription
    ) {
        TODO("Not yet implemented")
    }

    override fun getPolyPoints(featureId: Int): MutableList<MapPoint> {
        TODO("Not yet implemented")
    }

    override fun clearFeatures() {
    }

    override fun clearFeatures(ids: List<Int>) {
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

    override fun setGpsLocationListener(listener: MapFragment.PointListener?) {
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
    }

    override fun hasCenter(): Boolean {
        return false
    }
}
