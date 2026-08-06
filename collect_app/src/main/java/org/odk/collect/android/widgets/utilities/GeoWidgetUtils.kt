package org.odk.collect.android.widgets.utilities

import android.content.Context
import org.odk.collect.maps.MapPoint
import org.odk.collect.shared.strings.StringUtils.removeEnd
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.absoluteValue

object GeoWidgetUtils {

    @JvmStatic
    fun getGeoPointAnswerToDisplay(context: Context, answer: String?): String {
        try {
            if (answer != null && answer.isNotEmpty()) {
                val parts = answer.split(" ").toTypedArray()
                return context.getString(
                    org.odk.collect.strings.R.string.gps_result,
                    formatCoordinate(parts[0].toDouble()),
                    formatCoordinate(parts[1].toDouble()),
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

    fun isWithinMapBounds(point: MapPoint): Boolean {
        return point.latitude.absoluteValue <= 90 && point.longitude.absoluteValue <= 180
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

    private fun formatCoordinate(coordinate: Double): String {
        return DecimalFormat("0.000000").format(coordinate) + "°"
    }
}
