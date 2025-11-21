package org.odk.collect.geo.geopoly

import org.odk.collect.geo.GeoUtils.parseGeometryPoint
import org.odk.collect.maps.MapPoint
import kotlin.math.max
import kotlin.math.min

object GeoPolyUtils {

    fun parseGeometry(geometry: String?, strict: Boolean = false): List<MapPoint> {
        val points = ArrayList<MapPoint>()

        for (vertex in (geometry ?: "").split(";")) {
            val point = parseGeometryPoint(vertex, strict)
            if (point != null) {
                points.add(MapPoint(point[0], point[1], point[2], point[3]))
            } else {
                return ArrayList()
            }
        }

        return points
    }

    /**
     * Returns `true` if any segment of the trace intersects with any other and `false` otherwise.
     */
    fun intersects(trace: List<MapPoint>): Boolean {
        return if (trace.size >= 3) {
            val isClosed = trace.isNotEmpty() && trace.first() == trace.last()
            val segments = trace.zipWithNext()
            segments.filterIndexed { line1Index, line1 ->
                segments.filterIndexed { line2Index, line2 ->
                    if (isClosed && line1Index == 0 && line2Index == segments.size - 1) {
                        false
                    } else if (line2Index >= line1Index + 2) {
                        intersects(line1, line2)
                    } else {
                        false
                    }
                }.isNotEmpty()
            }.isNotEmpty()
        } else {
            false
        }
    }

    /**
     * Check if a point is within the bounding box of a line
     */
    fun within(
        point: MapPoint,
        line: Pair<MapPoint, MapPoint>
    ): Boolean {
        val lineLatMin = min(line.first.latitude, line.second.latitude)
        val lineLatMax = max(line.first.latitude, line.second.latitude)
        val lineLongMin = min(line.first.longitude, line.second.longitude)
        val lineLongMax = max(line.first.longitude, line.second.longitude)
        val latRange = lineLatMin..lineLatMax
        val longRange = lineLongMin..lineLongMax

        return point.latitude in latRange && point.longitude in longRange
    }

    /**
     * Work out whether two line segments intersect by calculating if the endpoints of one segment
     * are on opposite sides (or touching of the other segment **and** vice versa. This is
     * determined by finding the orientation of endpoints relative to the other line.
     */
    private fun intersects(
        aB: Pair<MapPoint, MapPoint>,
        cD: Pair<MapPoint, MapPoint>
    ): Boolean {
        val (a, b) = aB
        val (c, d) = cD

        val orientationA = orientation(a, c, d)
        val orientationB = orientation(b, c, d)
        val orientationC = orientation(a, b, c)
        val orientationD = orientation(a, b, d)

        return if (orientationA.isOpposing(orientationB) && orientationC.isOpposing(orientationD)) {
            true
        } else if (orientationA == Orientation.Collinear && within(a, cD)) {
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
    private fun orientation(a: MapPoint, b: MapPoint, c: MapPoint): Orientation {
        val ab = Pair(b.latitude - a.latitude, b.longitude - a.longitude)
        val ac = Pair(c.latitude - a.latitude, c.longitude - a.longitude)
        val crossProduct = crossProduct(ab, ac)

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
}
