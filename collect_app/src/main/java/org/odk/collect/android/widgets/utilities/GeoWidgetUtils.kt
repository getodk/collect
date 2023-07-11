package org.odk.collect.android.widgets.utilities

import android.content.Context
import android.location.Location
import org.odk.collect.android.R
import org.odk.collect.maps.MapPoint
import org.odk.collect.shared.strings.StringUtils.removeEnd
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.absoluteValue

object GeoWidgetUtils {

    @JvmStatic
    fun getGeoPointAnswerToDisplay(context: Context, answer: String?): String {
        try {
            if (answer != null && answer.isNotEmpty()) {
                val parts = answer.split(" ").toTypedArray()
                return context.getString(
                    org.odk.collect.strings.R.string.gps_result,
                    convertCoordinatesIntoDegreeFormat(context, parts[0].toDouble(), "lat"),
                    convertCoordinatesIntoDegreeFormat(context, parts[1].toDouble(), "lon"),
                    truncateDouble(parts[2]),
                    truncateDouble(parts[3])
                )
            }
        } catch (e: NumberFormatException) {
            return ""
        }

        return ""
    }

    @JvmStatic
    fun getGeoPolyAnswerToDisplay(answer: String?): String? {
        return if (answer != null && answer.isNotEmpty()) {
            removeEnd(answer.trim(), ";")
        } else {
            answer
        }
    }

    @JvmStatic
    fun parseGeometryPoint(answer: String?): DoubleArray? {
        if (answer != null && answer.isNotEmpty()) {
            val sa = answer.trim { it <= ' ' }.split(" ").toTypedArray()
            return try {
                doubleArrayOf(
                    sa[0].toDouble(),
                    if (sa.size > 1) sa[1].toDouble() else 0.0,
                    if (sa.size > 2) sa[2].toDouble() else 0.0,
                    if (sa.size > 3) sa[3].toDouble() else 0.0
                )
            } catch (e: Throwable) {
                null
            }
        } else {
            return null
        }
    }

    fun parseGeometry(geometry: String?): ArrayList<MapPoint> {
        val points = ArrayList<MapPoint>()

        for (vertex in (geometry ?: "").split(";").toTypedArray()) {
            val point = parseGeometryPoint(vertex)
            if (point != null) {
                points.add(MapPoint(point[0], point[1], point[2], point[3]))
            } else {
                return ArrayList()
            }
        }

        return points
    }

    fun isWithinMapBounds(point: MapPoint): Boolean {
        return point.latitude.absoluteValue <= 90 && point.longitude.absoluteValue <= 180
    }

    @JvmStatic
    fun convertCoordinatesIntoDegreeFormat(
        context: Context,
        coordinate: Double,
        type: String
    ): String {
        val coordinateDegrees = Location.convert(abs(coordinate), Location.FORMAT_SECONDS)
        val coordinateSplit = coordinateDegrees.split(":").toTypedArray()
        val degrees = floor(coordinateSplit[0]) + "°"
        val mins = floor(coordinateSplit[1]) + "'"
        val secs = floor(coordinateSplit[2]) + '"'
        return String.format(getCardinalDirection(context, coordinate, type), degrees, mins, secs)
    }

    @JvmStatic
    fun floor(value: String?): String {
        if (value == null || value.isEmpty()) {
            return ""
        }
        return if (value.contains(".")) value.substring(0, value.indexOf('.')) else value
    }

    @JvmStatic
    fun truncateDouble(string: String?): String {
        val df = DecimalFormat("#.##")
        try {
            return df.format(string?.toDouble())
        } catch (e: Throwable) {
            Timber.w(e)
        }
        return ""
    }

    private fun getCardinalDirection(context: Context, coordinate: Double, type: String): String {
        return if (type.equals("lon", ignoreCase = true)) {
            if (coordinate < 0) {
                context.getString(org.odk.collect.strings.R.string.west)
            } else {
                context.getString(org.odk.collect.strings.R.string.east)
            }
        } else if (coordinate < 0) {
            context.getString(org.odk.collect.strings.R.string.south)
        } else {
            context.getString(org.odk.collect.strings.R.string.north)
        }
    }
}
