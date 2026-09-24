package org.odk.collect.android.widgets.utilities

import android.content.Context
import android.icu.text.DecimalFormat
import org.odk.collect.maps.MapPoint
import org.odk.collect.shared.strings.StringUtils.removeEnd
import kotlin.math.absoluteValue

object GeoWidgetUtils {

    @JvmStatic
    fun getGeoPointAnswerToDisplay(context: Context, answer: String?): String {
        if (answer.isNullOrEmpty()) {
            return ""
        }

        return try {
            val parts = answer.split(" ")
            context.getString(
                org.odk.collect.strings.R.string.gps_result,
                formatCoordinate(parts[0].toDouble()),
                formatCoordinate(parts[1].toDouble()),
                formatAltitude(parts[2].toDouble()),
                formatAccuracy(parts[3].toDouble())
            )
        } catch (_: Exception) {
            ""
        }
    }

    @JvmStatic
    fun getGeoPolyAnswerToDisplay(answer: String?): String? {
        return if (!answer.isNullOrEmpty()) {
            removeEnd(answer.trim(), ";")
        } else {
            answer
        }
    }

    fun isWithinMapBounds(point: MapPoint): Boolean {
        return point.latitude.absoluteValue <= 90 && point.longitude.absoluteValue <= 180
    }

    private fun formatCoordinate(coordinate: Double): String {
        return DecimalFormat("0.000000").format(coordinate)
    }

    private fun formatAltitude(altitude: Double): String {
        return DecimalFormat("0.#").format(altitude)
    }

    private fun formatAccuracy(accuracy: Double): String {
        // At most two significant digits
        return DecimalFormat("@#").format(accuracy)
    }
}
