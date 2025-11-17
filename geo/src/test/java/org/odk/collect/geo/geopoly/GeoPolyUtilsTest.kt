package org.odk.collect.geo.geopoly

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
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
}
