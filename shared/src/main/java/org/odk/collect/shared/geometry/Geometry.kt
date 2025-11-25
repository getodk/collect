package org.odk.collect.shared.geometry

import kotlin.math.max
import kotlin.math.min

data class Point(val x: Double, val y: Double)
data class LineSegment(val start: Point, val end: Point)
data class Trace(val points: List<Point>) {
    fun isClosed(): Boolean {
        return points.first() == points.last()
    }
}

fun Trace.segments(): List<LineSegment> {
    return points.zipWithNext().map { (start, end) -> LineSegment(start, end) }
}

/**
 * Returns `true` if any segment of the trace intersects with any other and `false` otherwise.
 */
fun Trace.intersects(): Boolean {
    val points = this.points
    return if (points.size >= 3) {
        val segments = segments()
        return if (segments.size == 2) {
            val orientation = orientation(segments[1].end, segments[0].start, segments[0].end)
            orientation == Orientation.Collinear && segments[1].end.within(segments[0])
        } else {
            segments.filterIndexed { line1Index, line1 ->
                segments.filterIndexed { line2Index, line2 ->
                    if (isClosed() && line1Index == 0 && line2Index == segments.size - 1) {
                        false
                    } else if (line2Index >= line1Index + 2) {
                        line1.intersects(line2)
                    } else {
                        false
                    }
                }.isNotEmpty()
            }.isNotEmpty()
        }
    } else {
        false
    }
}

/**
 * Check if a point is within the bounding box defined by a line between two non-consecutive corners
 */
fun Point.within(segment: LineSegment): Boolean {
    val lineXMin = min(segment.start.x, segment.end.x)
    val lineXMax = max(segment.start.x, segment.end.x)
    val lineYMin = min(segment.start.y, segment.end.y)
    val lineYMax = max(segment.start.y, segment.end.y)
    val xRange = lineXMin..lineXMax
    val yRange = lineYMin..lineYMax

    return x in xRange && y in yRange
}

/**
 * Work out whether two line segments intersect by calculating if the endpoints of one segment
 * are on opposite sides (or touching of the other segment **and** vice versa. This is
 * determined by finding the orientation of endpoints relative to the other line.
 */
fun LineSegment.intersects(other: LineSegment): Boolean {
    val (a, b) = this
    val (c, d) = other

    val orientationA = orientation(a, c, d)
    val orientationB = orientation(b, c, d)
    val orientationC = orientation(a, b, c)
    val orientationD = orientation(a, b, d)

    return if (orientationA.isOpposing(orientationB) && orientationC.isOpposing(orientationD)) {
        true
    } else if (orientationA == Orientation.Collinear && a.within(other)) {
        true
    } else if (orientationB == Orientation.Collinear && b.within(other)) {
        true
    } else if (orientationC == Orientation.Collinear && c.within(other)) {
        true
    } else if (orientationD == Orientation.Collinear && d.within(other)) {
        true
    } else {
        false
    }
}

/**
 * Calculate the "orientation" (or "direction") of three points using the cross product of the
 * vectors of the pairs of points (see
 * [here](https://en.wikipedia.org/wiki/Cross_product#Computational_geometry)). This can
 * either be clockwise, anticlockwise or collinear (the three points form a straight line).
 *
 */
private fun orientation(a: Point, b: Point, c: Point): Orientation {
    val crossProduct = crossProduct(Pair(b.x - a.x, b.y - a.y), Pair(c.x - a.x, c.y - a.y))
    return if (crossProduct > 0) {
        Orientation.AntiClockwise
    } else if (crossProduct < 0) {
        Orientation.Clockwise
    } else {
        Orientation.Collinear
    }
}

/**
 * [https://en.wikipedia.org/wiki/Cross_product](https://en.wikipedia.org/wiki/Cross_product)
 */
private fun crossProduct(x: Pair<Double, Double>, y: Pair<Double, Double>): Double {
    return (x.first * y.second) - (y.first * x.second)
}

private enum class Orientation {
    Collinear,
    Clockwise,
    AntiClockwise;

    fun isOpposing(other: Orientation): Boolean {
        return if (this == Collinear) {
            false
        } else if (this == Clockwise && other == AntiClockwise) {
            true
        } else if (this == AntiClockwise && other == Clockwise) {
            true
        } else {
            false
        }
    }
}
