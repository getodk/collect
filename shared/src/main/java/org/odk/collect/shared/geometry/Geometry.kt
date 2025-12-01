package org.odk.collect.shared.geometry

import kotlin.math.abs
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
    return points.zipWithNext().flatMap { (start, end) ->
        if (start != end) {
            listOf(LineSegment(start, end))
        } else {
            emptyList()
        }
    }
}

/**
 * Returns `true` if any segment of the trace intersects with any other and `false` otherwise.
 */
fun Trace.intersects(epsilon: Double = 0.0): Boolean {
    val points = this.points
    return if (points.size >= 3) {
        val segments = segments()
        if (segments.size == 2) {
            segments[0].intersects(segments[1], allowConnection = true, epsilon = epsilon)
        } else {
            segments.filterIndexed { line1Index, line1 ->
                segments.filterIndexed { line2Index, line2 ->
                    if (isClosed() && line1Index == 0 && line2Index == segments.size - 1) {
                        false
                    } else if (line2Index == line1Index + 1) {
                        line1.intersects(line2, allowConnection = true, epsilon = epsilon)
                    } else if (line2Index >= line1Index + 2) {
                        line1.intersects(line2, epsilon = epsilon)
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
 *
 * @param allowConnection will allow the end of `this` and the start of `other` to intersect
 * provided they are equivalent (the two segments are "connected")
 */
fun LineSegment.intersects(other: LineSegment, allowConnection: Boolean = false, epsilon: Double = 0.0): Boolean {
    val (a, b) = this
    val (c, d) = other

    val orientationA = orientation(a, c, d, epsilon)
    val orientationD = orientation(a, b, d, epsilon)

    return if (orientationA == Orientation.Collinear && a.within(other)) {
        true
    } else if (orientationD == Orientation.Collinear && d.within(this)) {
        true
    } else if (b == c && allowConnection) {
        false
    } else {
        val orientationB = orientation(b, c, d, epsilon)
        val orientationC = orientation(a, b, c, epsilon)

        if (orientationA.isOpposing(orientationB) && orientationC.isOpposing(orientationD)) {
            true
        } else if (orientationB == Orientation.Collinear && b.within(other)) {
            true
        } else if (orientationC == Orientation.Collinear && c.within(this)) {
            true
        } else {
            false
        }
    }
}

/**
 * Calculate a [Point] on this [LineSegment] based on the `position` using
 * [Linear interpolation](https://en.wikipedia.org/wiki/Linear_interpolation). `0` will return
 * [LineSegment.start] and `1` will return [LineSegment.end].
 */
fun LineSegment.interpolate(position: Double): Point {
    val x = start.x + position * (end.x - start.x)
    val y = start.y + position * (end.y - start.y)
    return Point(x, y)
}

/**
 * Calculate the "orientation" (or "direction") of three points using the cross product of the
 * vectors of the pairs of points (see
 * [here](https://en.wikipedia.org/wiki/Cross_product#Computational_geometry)). This can
 * either be clockwise, anticlockwise or collinear (the three points form a straight line).
 *
 * @param epsilon the epsilon used to check for collinearity
 *
 */
fun orientation(a: Point, b: Point, c: Point, epsilon: Double = 0.0): Orientation {
    val crossProduct = crossProduct(Pair(b.x - a.x, b.y - a.y), Pair(c.x - a.x, c.y - a.y))
    return if (abs(crossProduct) <= epsilon) {
        Orientation.Collinear
    } else if (crossProduct > 0) {
        Orientation.AntiClockwise
    } else {
        Orientation.Clockwise
    }
}

/**
 * [https://en.wikipedia.org/wiki/Cross_product](https://en.wikipedia.org/wiki/Cross_product)
 */
private fun crossProduct(x: Pair<Double, Double>, y: Pair<Double, Double>): Double {
    return (x.first * y.second) - (y.first * x.second)
}

enum class Orientation {
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
