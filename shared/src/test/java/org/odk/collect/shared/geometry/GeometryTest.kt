package org.odk.collect.shared.geometry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import kotlin.random.Random

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
    fun `Trace#intersects returns true when two segments and they intersect`() {
        val endpointWithin = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(0.0, 1.0),
                Point(0.0, 0.5)
            )
        )
        assertThat(endpointWithin.intersects(), equalTo(true))

        val endpointBeyond = Trace(listOf(
            Point(0.0, 0.0),
            Point(0.0, 1.0),
            Point(0.0, -1.0),
        ))
        assertThat(endpointBeyond.intersects(), equalTo(true))

        val endpointMatching = Trace(listOf(
            Point(0.0, 0.0),
            Point(0.0, 1.0),
            Point(0.0, 0.0),
        ))
        assertThat(endpointMatching.intersects(), equalTo(true))
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

    @Test
    fun `Trace#segments returns false for trace with duplicate points`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(1.0, 0.0),
                Point(1.0, 0.0),
                Point(2.0, 0.0)
            )
        )

        assertThat(trace.intersects(), equalTo(false))
    }

    @Test
    fun `Trace#segments does not include zero-length segments`() {
        val trace = Trace(
            listOf(
                Point(0.0, 0.0),
                Point(1.0, 0.0),
                Point(1.0, 0.0),
                Point(2.0, 0.0)
            )
        )

        assertThat(trace.segments(), equalTo(listOf(
            LineSegment(Point(0.0, 0.0), Point(1.0, 0.0)),
            LineSegment(Point(1.0, 0.0), Point(2.0, 0.0))
        )))
    }

    @Test
    fun `Trace#intersects returns true for 3 segment closed trace reversing on itself`() {
        val trace = Trace(listOf(
            Point(0.0, 0.0),
            Point(2.0, 0.0),
            Point(1.0, 0.0),
            Point(0.0, 0.0)
        ))
        assertThat(trace.intersects(), equalTo(true))
    }

    @Test
    fun `Trace#intersects satisfies metamorphic relationships`() {
        0.until(1000).map {
            val trace = generateTrace()
            val intersects = trace.intersects()

            // Check intersects is consistent when trace is reversed
            val reversedTrace = Trace(trace.points.reversed())
            assertThat(
                "Expected intersects=$intersects:\n$reversedTrace",
                reversedTrace.intersects(),
                equalTo(intersects)
            )

            // Check intersects is consistent when trace is scaled
            val scaleFactor = Random.nextDouble(0.1, 10.0)
            val scaledTrace = Trace(trace.points.map {
                Point(it.x * scaleFactor, it.y * scaleFactor)
            })
            assertThat(
                "Expected intersects=$intersects:\n$scaledTrace",
                scaledTrace.intersects(),
                equalTo(intersects)
            )

            // Check adding an intersection makes intersects true
            if (!intersects) {
                val intersectPosition = Random.nextDouble(0.0, 1.0)
                val intersectionPoint = trace.segments().first().interpolate(intersectPosition)
                val intersectingTrace =
                    Trace(trace.points + listOf(trace.points.last(), intersectionPoint))
                assertThat(
                    "Expected intersect=true:\n$intersectingTrace",
                    intersectingTrace.intersects(),
                    equalTo(true)
                )
            }
        }
    }

    @Test
    fun `LineSegment#intersects detects any endpoint touching the other line`() {
        val line = LineSegment(Point(0.0, 0.0), Point(0.0, 2.0))

        val aTouching = LineSegment(Point(-1.0, 0.0), Point(1.0, 0.0))
        assertThat(line.intersects(aTouching), equalTo(true))

        val bTouching = LineSegment(Point(-1.0, 2.0), Point(1.0, 2.0))
        assertThat(line.intersects(bTouching), equalTo(true))

        val cTouching = LineSegment(Point(0.0, 1.0), Point(1.0, 1.0))
        assertThat(line.intersects(cTouching), equalTo(true))

        val dTouching = LineSegment(Point(-1.0, 1.0), Point(0.0, 1.0))
        assertThat(line.intersects(dTouching), equalTo(true))
    }

    @Test
    fun `LineSegment#intersects does not detect intersections for collinear endpoints`() {
        val segment1 = LineSegment(Point(0.0, 0.0), Point(4.0, 0.0))
        val segment2 = LineSegment(Point(4.0, 4.0), Point(8.0, 0.0))

        assertThat(segment1.intersects(segment2), equalTo(false))
    }

    @Test
    fun `LineSegment#intersects with allowConnection true still finds intersections in a cross`() {
        val segment1 = LineSegment(Point(-1.0, 0.0), Point(1.0, 0.0))
        val segment2 = LineSegment(Point(0.0, -1.0), Point(0.0, 1.0))

        assertThat(segment1.intersects(segment2, allowConnection = true), equalTo(true))
    }

    private fun generateTrace(maxLength: Int = 10, maxCoordinate: Double = 100.0): Trace {
        val length = Random.nextInt(2, maxLength)
        val trace = Trace(0.until(length).map {
            Point(
                Random.nextDouble(maxCoordinate * -1, maxCoordinate),
                Random.nextDouble(maxCoordinate * -1, maxCoordinate)
            )
        })

        return if (trace.isClosed()) {
            trace
        } else {
            val shouldClose = Random.nextBoolean()
            if (shouldClose) {
                trace.copy(points = trace.points + trace.points.first())
            } else {
                trace
            }
        }
    }
}
