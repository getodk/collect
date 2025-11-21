package org.odk.collect.shared.geometry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class GeometryTest {
    @Test
    fun `Trace#intersects returns false for an empty list`() {
        assertThat(Trace(emptyList()).intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns false when there is only one point`() {
        val trace = Trace(listOf(Point(0.0, 0.0)))
        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns false when there is only one segment`() {
        val trace = Trace(listOf(Point(0.0, 0.0), Point(1.0, 0.0)))
        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns false when no segment intersects with another`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(1.0, 1.0),
                Point(2.0, 0.0)
            )
        )

        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns false when no segment intersects with another in a closed trace`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(1.0, 1.0),
                Point(2.0, 0.0),
                Point(0.0, 0.0)
            )
        )

        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns true when a segment intersects with another`() {
        val trace = Trace(
            listOf(
                Point(1.0, 1.0),
                Point(1.0, 3.0),
                Point(2.0, 3.0),
                Point(2.0, 2.0),
                Point(0.0, 2.0)
            )
        )

        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects returns true when a segment intersects with another in a closed trace`() {
        val trace = Trace(
            listOf(
                Point(1.0, 1.0),
                Point(1.0, 3.0),
                Point(2.0, 3.0),
                Point(2.0, 2.0),
                Point(0.0, 2.0),
                Point(1.0, 1.0)
            )
        )

        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects returns false when a segment's end points are both on different sides of another, but the segments do not intersect`() {
        val trace = Trace(
            listOf(
                Point(1.0, 1.0),
                Point(1.0, 2.0),
                Point(3.0, 3.0),
                Point(0.0, 3.0)
            )
        )

        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#intersects returns true when just an endpoint touches another segment`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(1.0, 1.0),
                Point(2.0, 0.0),
                Point(-1.0, 0.0)
            )
        )

        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects returns true when a segment is collinear and within another`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(0.0, 1.0),
                Point(0.0, 0.5)
            )
        )

        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects returns true when the trace closes on a non-origin vertex`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(0.0, 1.0), // Close back on this point
                Point(0.0, 2.0),
                Point(1.0, 2.0),
                Point(0.0, 1.0)
            )
        )

        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects returns false for right angled triangle`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(10.0, 10.0),
                Point(0.0, 10.0)
            )
        )

        assertThat(trace.intersects(), equalTo(false))
    }
}
