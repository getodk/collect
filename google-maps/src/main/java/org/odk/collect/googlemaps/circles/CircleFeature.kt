package org.odk.collect.googlemaps.circles

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.googlemaps.MapPointExt.toLatLng
import org.odk.collect.maps.circles.CircleDescription
import org.odk.collect.maps.circles.getFillColor
import org.odk.collect.maps.circles.getStrokeColor

class CircleFeature(circleDescription: CircleDescription, map: GoogleMap) :
    GoogleMapFragment.MapFeature {

    private val circle = map.addCircle(
        CircleOptions()
            .center(circleDescription.center.toLatLng())
            .radius(circleDescription.radius.toDouble())
            .strokeColor(circleDescription.getStrokeColor())
            .fillColor(circleDescription.getFillColor())
            .strokeWidth(1f)
    )

    override fun ownsMarker(marker: Marker?): Boolean {
        return false
    }

    override fun ownsPolyline(polyline: Polyline?): Boolean {
        return false
    }

    override fun ownsPolygon(polygon: Polygon?): Boolean {
        return false
    }

    override fun update() {

    }

    override fun dispose() {
        circle.remove()
    }
}