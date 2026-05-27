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
}
