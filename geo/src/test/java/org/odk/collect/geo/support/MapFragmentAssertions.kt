package org.odk.collect.geo.support

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.circles.CurrentLocationDelegate

object MapFragmentAssertions {
    fun hasZoomedToCurrentLocation(location: MapPoint): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                return mapFragment.getCenter() == location && mapFragment.getZoom() != 0.0
            }

            override fun describeTo(description: Description) {
                description.appendText("is zoomed to $location")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("was ${mapFragment.getCenter()}")
            }
        }
    }

    fun showsCurrentLocation(location: MapPoint): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val hasLocationMarker = mapFragment.getMarkers().any {
                    it.point == location && it.iconDescription == CurrentLocationDelegate.ICON_DESCRIPTION
                }

                val hasAccuracyCircle = mapFragment.getCircles().any {
                    it.center == location && it.radius == location.accuracy.toFloat()
                }

                return hasLocationMarker && hasAccuracyCircle
            }

            override fun describeTo(description: Description) {
                description.appendText("shows current location as $location")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("did not show current location")
            }
        }
    }

    fun showsMappablePoints(items: List<MappableItem.Point>): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val markerPoints = mapFragment.getMarkers().map { it.point }
                return markerPoints == items.map { it.point }
            }

            override fun describeTo(description: Description) {
                description.appendText("shows markers for $items")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("was ${mapFragment.getMarkers()}")
            }
        }
    }

    fun showsMappableLines(
        items: List<MappableItem.Line>,
        strokeWidth: Float,
        strokeColor: Int
    ): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val polyLines = mapFragment.getPolyLines()

                val pointsMatch = polyLines.map { it.points } == items.map { it.points }
                val styleIsCorrect = polyLines.all {
                    it.getStrokeWidth() == strokeWidth && it.getStrokeColor() == strokeColor
                }

                return pointsMatch && styleIsCorrect
            }

            override fun describeTo(description: Description) {
                description.appendText("shows polylines for $items")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("was ${mapFragment.getPolyLines()}")
            }
        }
    }

    fun showsMappablePolygons(
        items: List<MappableItem.Polygon>,
        strokeWidth: Float,
        strokeColor: Int,
        fillColor: Int
    ): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val polyLines = mapFragment.getPolygons()

                val pointsMatch = polyLines.map { it.points } == items.map { it.points }
                val styleIsCorrect = polyLines.all {
                    it.getStrokeWidth() == strokeWidth &&
                            it.getStrokeColor() == strokeColor &&
                            it.getFillColor() == fillColor
                }

                return pointsMatch && styleIsCorrect
            }

            override fun describeTo(description: Description) {
                description.appendText("shows polylines for $items")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("was ${mapFragment.getPolyLines()}")
            }
        }
    }
}