package org.odk.collect.maps.circles

import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

/**
 * Helper for rendering a current location icon with accuracy "halo" on a [MapFragment] as well
 * as zooming/moving the map on location updates.
 */
class CurrentLocationDelegate {

    private var currentLocation: MapPoint? = null
    private var locationMarkerId: Int? = null
    private var accuracyHaloId: Int? = null

    fun update(map: MapFragment, location: MapPoint, follow: Boolean) {
        currentLocation = location

        val markerDescription = MarkerDescription(
            location,
            false,
            MapFragment.IconAnchor.CENTER,
            MarkerIconDescription.DrawableResource(
                org.odk.collect.maps.R.drawable.ic_current_location,
                MapConsts.DEFAULT_STROKE_COLOR
            )
        )

        locationMarkerId.let {
            if (it == null) {
                locationMarkerId = map.addMarker(markerDescription)
            } else {
                map.updateMarker(it, markerDescription)
            }
        }


        val circleDescription = CircleDescription(location, location.accuracy.toFloat())
        accuracyHaloId.let {
            if (it == null) {
                accuracyHaloId = map.addCircle(circleDescription)
            } else {
                map.updateCircle(it, circleDescription)
            }
        }

        if (!map.hasCenter()) {
            map.zoomToCurrentLocation(location)
        }

        if (follow) {
            map.setCenter(location, false)
        }
    }

    fun zoomToCurrentLocation(map: MapFragment) {
        map.zoomToCurrentLocation(currentLocation)
    }
}