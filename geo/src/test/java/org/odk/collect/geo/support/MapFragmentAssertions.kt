package org.odk.collect.geo.support

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.geo.items.MappableData
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
                val markers = mapFragment.getMarkers()
                return items.all { item -> markers.any { it.point == item.point } }
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
        strokeWidth: Float? = null,
        strokeColor: Int? = null
    ): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val polyLines = mapFragment.getPolyLines()

                return items.all { item ->
                    polyLines.any {
                        item.points == it.points &&
                                (strokeWidth == null || it.getStrokeWidth() == strokeWidth) &&
                                (strokeColor == null || it.getStrokeColor() == strokeColor)
                    }
                }
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
        strokeWidth: Float? = null,
        strokeColor: Int? = null,
        fillColor: Int? = null
    ): TypeSafeMatcher<FakeMapFragment> {
        return object : TypeSafeMatcher<FakeMapFragment>() {
            override fun matchesSafely(mapFragment: FakeMapFragment): Boolean {
                val polygons = mapFragment.getPolygons()
                return items.all { item ->
                    polygons.any {
                        item.points == it.points &&
                                (strokeWidth == null || it.getStrokeWidth() == strokeWidth) &&
                                (strokeColor == null || it.getStrokeColor() == strokeColor) &&
                                (fillColor == null || it.getFillColor() == fillColor)
                    }
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("shows polygons for $items")
            }

            override fun describeMismatchSafely(
                mapFragment: FakeMapFragment,
                mismatchDescription: Description
            ) {
                mismatchDescription.appendText("was ${mapFragment.getPolygons()}")
            }
        }
    }

    fun showsMappableData(
        mappableData: MappableData,
        lineStrokeWidth: Float? = null,
        lineStrokeColor: Int? = null,
        polygonStrokeWidth: Float? = null,
        polygonStrokeColor: Int? = null,
        polygonFillColor: Int? = null
    ): Matcher<FakeMapFragment> {
        val items = mappableData.getMappableItems().value
        val points = items?.filterIsInstance<MappableItem.Point>() ?: emptyList()
        val lines = items?.filterIsInstance<MappableItem.Line>() ?: emptyList()
        val polygons = items?.filterIsInstance<MappableItem.Polygon>() ?: emptyList()

        return allOf(
            showsMappablePoints(points),
            showsMappableLines(lines, lineStrokeWidth, lineStrokeColor),
            showsMappablePolygons(
                polygons,
                polygonStrokeWidth,
                polygonStrokeColor,
                polygonFillColor
            )
        )
    }
}