package org.odk.collect.geo.support

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.maps.MapPoint

object MapFragmentAssertions {
    fun hasZoomedToCurrentLocation(location: MapPoint): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                return mapFragment.getCenter() == location && mapFragment.getZoom() != 0.0
            }

            override fun describeTo(description: Description) {
                description.appendText("is zoomed to $location")
            }
        }
    }

    fun showsCurrentLocation(location: MapPoint): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val hasLocationMarker = mapFragment.getMarkers().contains(location)
                val hasAccuracyCircle = mapFragment.getCircles().any {
                    it.center == location && it.radius == location.accuracy.toFloat()
                }

                return hasLocationMarker && hasAccuracyCircle
            }

            override fun describeTo(description: Description) {
                description.appendText("shows current location as $location")
            }
        }
    }
}