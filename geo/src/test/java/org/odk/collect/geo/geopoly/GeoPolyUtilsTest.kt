package org.odk.collect.geo.geopoly

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.maps.MapPoint

class GeoPolyUtilsTest {

    @Test
    fun `#intersects returns false for an empty list`() {
        assertThat(GeoPolyUtils.intersects(emptyList()), equalTo(false))
    }

    @Test
    fun `#intersects returns false when there is only one point`() {
        val trace = listOf(MapPoint(0.0, 0.0))
        assertThat(GeoPolyUtils.intersects(trace), equalTo(false))
    }

    @Test
    fun `#intersects returns false when there is only one segment`() {
        val trace = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 0.0))
        assertThat(GeoPolyUtils.intersects(trace), equalTo(false))
    }

    @Test
    fun `#intersects returns false when no segment intersects with another`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(1.0, 1.0),
            MapPoint(2.0, 0.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(false))
    }

    @Test
    fun `#intersects returns false when no segment intersects with another in a closed trace`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(1.0, 1.0),
            MapPoint(2.0, 0.0),
            MapPoint(0.0, 0.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(false))
    }

    @Test
    fun `#intersects returns true when a segment intersects with another`() {
        val trace = listOf(
            MapPoint(1.0, 1.0),
            MapPoint(1.0, 3.0),
            MapPoint(2.0, 3.0),
            MapPoint(2.0, 2.0),
            MapPoint(0.0, 2.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(true))
    }

    @Test
    fun `#intersects returns true when a segment intersects with another in a closed trace`() {
        val trace = listOf(
            MapPoint(1.0, 1.0),
            MapPoint(1.0, 3.0),
            MapPoint(2.0, 3.0),
            MapPoint(2.0, 2.0),
            MapPoint(0.0, 2.0),
            MapPoint(1.0, 1.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(true))
    }

    @Test
    fun `#intersects returns false when a segment's end points are both on different sides of another, but the segments do not intersect`() {
        val trace = listOf(
            MapPoint(1.0, 1.0),
            MapPoint(1.0, 2.0),
            MapPoint(3.0, 3.0),
            MapPoint(0.0, 3.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(false))
    }

    @Test
    fun `#intersects returns true when just an endpoint touches another segment`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(1.0, 1.0),
            MapPoint(2.0, 0.0),
            MapPoint(-1.0, 0.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(true))
    }

    @Test
    fun `#intersects returns true when a segment is collinear and within another`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(0.0, 1.0),
            MapPoint(0.0, 0.5)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(true))
    }

    @Test
    fun `#intersects returns true when the trace closes on a non-origin vertex`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(0.0, 1.0), // Close back on this point
            MapPoint(0.0, 2.0),
            MapPoint(1.0, 2.0),
            MapPoint(0.0, 1.0)
        )

        assertThat(GeoPolyUtils.intersects(trace), equalTo(true))
    }

    @Test
    fun parseGeometryTest() {
        assertThat(parseGeometry("1.0 2.0 3 4"), equalTo(listOf(MapPoint(1.0, 2.0, 3.0, 4.0))))
        assertThat(
            parseGeometry("1.0 2.0 3 4; 5.0 6.0 7 8"),
            equalTo(listOf(MapPoint(1.0, 2.0, 3.0, 4.0), MapPoint(5.0, 6.0, 7.0, 8.0)))
        )

        assertThat(parseGeometry("blah"), equalTo(emptyList()))
        assertThat(parseGeometry("1.0 2.0 3 4; blah"), equalTo(emptyList()))
        assertThat(
            parseGeometry("37.45153333333334 -122.15539166666667 0.0 qwerty"),
            equalTo(emptyList())
        )
    }
}
