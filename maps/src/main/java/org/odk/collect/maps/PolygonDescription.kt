package org.odk.collect.maps

import androidx.core.graphics.ColorUtils
import org.odk.collect.androidshared.utils.toColorInt

data class PolygonDescription(
    val points: List<MapPoint> = emptyList(),
    private val strokeWidth: String? = null,
    private val strokeColor: String? = null,
    private val fillColor: String? = null
) {
    fun getStrokeWidth(): Float {
        return try {
            strokeWidth?.toFloat()?.let {
                if (it >= 0) {
                    it
                } else {
                    MapConsts.DEFAULT_STROKE_WIDTH
                }
            } ?: MapConsts.DEFAULT_STROKE_WIDTH
        } catch (e: NumberFormatException) {
            MapConsts.DEFAULT_STROKE_WIDTH
        }
    }

    fun getStrokeColor(): Int {
        val customColor = strokeColor?.toColorInt()
        return customColor ?: MapConsts.DEFAULT_STROKE_COLOR
    }

    fun getFillColor(): Int {
        val customColor = fillColor?.toColorInt()?.let {
            ColorUtils.setAlphaComponent(
                it,
                MapConsts.DEFAULT_FILL_COLOR_OPACITY
            )
        }

        return customColor
            ?: ColorUtils.setAlphaComponent(
                MapConsts.DEFAULT_STROKE_COLOR,
                MapConsts.DEFAULT_FILL_COLOR_OPACITY
            )
    }
}
