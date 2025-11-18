package org.odk.collect.geo.geopoly

import org.odk.collect.geo.GeoUtils.parseGeometryPoint
import org.odk.collect.maps.MapPoint

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
            val segments = trace.zipWithNext()
            segments.any { line1 ->
                segments.any { line2 ->
                    intersects(line1, line2)
                }
            }
        } else {
            false
        }
    }

    /**
     * Work out whether two line segments intersect by calculating if the endpoints of one segment
     * are on opposite sides of the other segment **and** vice versa. This is determined by finding
     * the orientation of endpoints relative to the other line.
     */
    private fun intersects(
        segment1: Pair<MapPoint, MapPoint>,
        segment2: Pair<MapPoint, MapPoint>
    ): Boolean {
        return orientation(segment1.first, segment2.first, segment2.second)
            .isOpposing(orientation(segment1.second, segment2.first, segment2.second)) &&
            orientation(segment1.first, segment1.second, segment2.first)
                .isOpposing(orientation(segment1.first, segment1.second, segment2.second))
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
