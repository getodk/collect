package org.odk.collect.geo.geopoly

import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometryPoint
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
    fun `#intersects returns false when there is just two of the same segment`() {
        val trace = listOf(
            MapPoint(0.0, 0.0),
            MapPoint(1.0, 0.0),
            MapPoint(1.0, 0.0),
            MapPoint(0.0, 0.0)
        )

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
    fun parseGeometryPointTest() {
        var gp =
            parseGeometryPoint("37.45153333333334 -122.15539166666667 0.0 20.0")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(-122.15539166666667, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(20.0, gp[3])

        gp = parseGeometryPoint("37.45153333333334")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(0.0, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(0.0, gp[3])

        gp = parseGeometryPoint(" 37.45153333333334 -122.15539166666667 0.0 ")!!
        assertEquals(37.45153333333334, gp[0])
        assertEquals(-122.15539166666667, gp[1])
        assertEquals(0.0, gp[2])
        assertEquals(0.0, gp[3])

        assertEquals(null, parseGeometryPoint("37.45153333333334 -122.15539166666667 0.0 qwerty"))
        assertEquals(null, parseGeometryPoint(""))
        assertEquals(null, parseGeometryPoint(null))
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
