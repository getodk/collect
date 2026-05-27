package org.odk.collect.geo

import android.content.Context
import android.location.Location
import org.javarosa.core.model.data.GeoPointData
import org.odk.collect.maps.MapPoint
import org.odk.collect.shared.strings.StringUtils.removeEnd
import org.odk.collect.strings.R
import java.text.DecimalFormat
import java.util.Locale

object GeoUtils {

    /**
     * Serializes a list of vertices into a string, in the format
     * appropriate for storing as the result of the form question.
     */
    @JvmStatic
    fun formatPointsResultString(points: MutableList<MapPoint>, isShape: Boolean): String? {
        if (isShape) {
            // Polygons are stored with a last point that duplicates the
            // first point.  Add this extra point if it's not already present.
            val count = points.size
            if (count > 1 && points[0] != points[count - 1]) {
                points.add(points[0])
            }
        }
        val result = StringBuilder()
        for (point in points) {
            // TODO(ping): Remove excess precision when we're ready for the output to change.
            result.append(
                String.format(
                    Locale.US, "%s %s %s %s;",
                    point.latitude.toString(), point.longitude.toString(),
                    point.altitude.toString(), point.accuracy.toFloat().toString()
                )
            )
        }

        return removeEnd(result.toString().trim(), ";")
    }

    @JvmStatic
    fun formatLocationResultString(location: Location): String {
        return formatLocationResultString(
            org.odk.collect.location.Location(
                location.latitude,
                location.longitude,
                location.altitude,
                location.accuracy
            )
        )
    }

    fun formatLocationResultString(location: org.odk.collect.location.Location): String {
        return String.format(
            "%s %s %s %s", location.latitude, location.longitude,
            location.altitude, location.accuracy
        )
    }

    fun formatAccuracy(context: Context, accuracy: Float): String {
        val formattedValue = DecimalFormat("#.##").format(accuracy.toDouble())
        return context.getString(R.string.accuracy_m, formattedValue)
    }

    @JvmStatic
    @JvmOverloads
    fun parseGeometryPoint(answer: String?, strict: Boolean = false): DoubleArray? {
        if (!answer.isNullOrEmpty()) {
            val sa = answer.trim().split(" ")
            return try {
                doubleArrayOf(
                    sa[0].toDouble(),
                    if (sa.size > 1) sa[1].toDouble() else 0.0,
                    if (sa.size > 2) sa[2].toDouble() else 0.0,
                    if (sa.size > 3) sa[3].toDouble() else 0.0
                )
            } catch (_: Throwable) {
                if (strict) {
                    throw IllegalArgumentException()
                } else {
                    null
                }
            }
        } else {
            return null
        }
    }

    @JvmStatic
    fun GeoPointData.toMapPoint(): MapPoint {
        return MapPoint(this.getPart(0), this.getPart(1), this.getPart(2), this.getPart(3))
    }

    fun MapPoint.toLocation(): org.odk.collect.location.Location {
        return org.odk.collect.location.Location(latitude, longitude, altitude, accuracy.toFloat())
    }
}
