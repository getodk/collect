package org.odk.collect.maps

import androidx.core.graphics.ColorUtils
import org.odk.collect.androidshared.utils.toColorInt

data class PolygonDescription(
    val points: List<MapPoint>,
    private val strokeWidth: String?,
    private val strokeColor: String?,
    private val fillColor: String?
) {
    fun getStrokeWidth(): Float {
        return try {
            strokeWidth?.toFloat()?.let {
                if (it >= 0) {
                    it
                } else {
                    MapConsts.POLYLINE_STROKE_WIDTH
                }
            } ?: MapConsts.POLYLINE_STROKE_WIDTH
        } catch (e: Throwable) {
            MapConsts.POLYLINE_STROKE_WIDTH
        }
    }

    fun getStrokeColor(): Int {
        val customColor = strokeColor?.toColorInt()
        return customColor ?: "#ffff0000".toColorInt()!!
    }

    fun getFillColor(): Int {
        val customColor = fillColor?.toColorInt()?.let {
            ColorUtils.setAlphaComponent(
                it,
                MapConsts.POLYGON_FILL_COLOR_OPACITY
            )
        }

        return customColor
            ?: ColorUtils.setAlphaComponent(
                "#ffff0000".toColorInt()!!,
                MapConsts.POLYGON_FILL_COLOR_OPACITY
            )
    }
}
