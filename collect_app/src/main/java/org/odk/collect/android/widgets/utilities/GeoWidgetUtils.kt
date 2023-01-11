package org.odk.collect.android.widgets.utilities

import android.content.Context
import android.location.Location
import org.odk.collect.android.R
import org.odk.collect.maps.MapPoint
import org.odk.collect.shared.strings.StringUtils.removeEnd
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.abs

object GeoWidgetUtils {

    @JvmStatic
    fun getGeoPointAnswerToDisplay(context: Context, answer: String?): String {
        try {
            if (answer != null && answer.isNotEmpty()) {
                val parts = answer.split(" ".toRegex()).toTypedArray()
                return context.getString(
                    R.string.gps_result,
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
            removeEnd(answer.replace(";\\s".toRegex(), ";"), ";")
        } else {
            answer
        }
    }

    @JvmStatic
    fun parseGeometryPoint(answer: String?): DoubleArray? {
        val gp = DoubleArray(4)
        if (answer != null && answer.isNotEmpty()) {
            val sa = answer.trim { it <= ' ' }.split(" ".toRegex()).toTypedArray()
            return try {
                gp[0] = sa[0].toDouble()
                gp[1] = sa[1].toDouble()
                gp[2] = sa[2].toDouble()
                gp[3] = sa[3].toDouble()
                gp
            } catch (e: Exception) {
                null
            } catch (e: Error) {
                null
            }
        } else {
            return null
        }
    }

    fun parseGeometry(geometry: String?): ArrayList<MapPoint> {
        val points = ArrayList<MapPoint>()

        for (vertex in (geometry ?: "").split(";".toRegex()).toTypedArray()) {
            val words = parseGeometryPoint(vertex)

            if (words != null && words.size >= 2) {
                var lat: Double
                var lon: Double
                var alt: Double
                var sd: Double
                try {
                    lat = words[0]
                    lon = words[1]
                    alt = if (words.size > 2) words[2] else 0.0
                    sd = if (words.size > 3) words[3] else 0.0
                } catch (e: NumberFormatException) {
                    continue
                }

                points.add(MapPoint(lat, lon, alt, sd))
            }
        }

        return points
    }

    @JvmStatic
    fun convertCoordinatesIntoDegreeFormat(
        context: Context,
        coordinate: Double,
        type: String,
    ): String {
        val coordinateDegrees = Location.convert(abs(coordinate), Location.FORMAT_SECONDS)
        val coordinateSplit = coordinateDegrees.split(":".toRegex()).toTypedArray()
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
    fun truncateDouble(s: String?): String {
        val df = DecimalFormat("#.##")
        try {
            return df.format(java.lang.Double.valueOf(s))
        } catch (e: Exception) {
            Timber.w(e)
        } catch (e: Error) {
            Timber.w(e)
        }
        return ""
    }

    private fun getCardinalDirection(context: Context, coordinate: Double, type: String): String {
        return if (type.equals("lon", ignoreCase = true)) {
            if (coordinate < 0) {
                context.getString(R.string.west)
            } else {
                context.getString(R.string.east)
            }
        } else if (coordinate < 0) {
            context.getString(R.string.south)
        } else {
            context.getString(R.string.north)
        }
    }
}
