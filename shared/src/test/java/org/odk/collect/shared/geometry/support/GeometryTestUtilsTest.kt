package org.odk.collect.shared.geometry.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.geometry.LineSegment
import org.odk.collect.shared.geometry.Orientation
import org.odk.collect.shared.geometry.Point
import org.odk.collect.shared.geometry.orientation
import org.odk.collect.shared.geometry.support.GeometryTestUtils.interpolate
import org.odk.collect.shared.geometry.within

class GeometryTestUtilsTest {

    @Test
    fun `LineSegment#interpolate returns a point on the segment at a proportional distance`() {
        val segment = LineSegment(Point(0.0, 0.0), Point(1.0, 0.0))

        assertThat(segment.interpolate(0.0), equalTo(Point(0.0, 0.0)))
        assertThat(segment.interpolate(0.5), equalTo(Point(0.5, 0.0)))
        assertThat(segment.interpolate(1.0), equalTo(Point(1.0, 0.0)))
    }

    @Test
    fun `LineSegment#interpolate returns a collinear point within the line's bounding box`() {
        val segment = LineSegment(Point(0.0, 0.0), Point(1.0, 1.0))
        val interpolatedPoint = segment.interpolate(0.5)

        val orientation = orientation(interpolatedPoint, segment.start, segment.end)
        assertThat(orientation, equalTo(Orientation.Collinear))
        assertThat(interpolatedPoint.within(segment), equalTo(true))
    }

    @Test
    fun `LineSegment#interpolate returns a collinear point within the line's bounding box for higher precision points with a suitable epsilon`() {
        val segment = LineSegment(Point(56.6029153, 20.2311124), Point(56.6029192, 20.2310467))
        val interpolatedPoint = segment.interpolate(0.5)

        val orientation = orientation(interpolatedPoint, segment.start, segment.end, epsilon = 0.000001)
        assertThat(orientation, equalTo(Orientation.Collinear))
        assertThat(interpolatedPoint.within(segment), equalTo(true))
    }
}
