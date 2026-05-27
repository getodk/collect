package org.odk.collect.maps

import androidx.core.graphics.ColorUtils

data class PolygonDescription(
    override val points: List<MapPoint> = emptyList(),
    private val strokeWidth: String? = null,
    private val strokeColor: Int? = null,
    private val fillColor: Int? = null,
    override val highlightLastPoint: Boolean = false,
    val draggable: Boolean = false
) : TraceDescription {
    override fun getStrokeWidth(): Float {
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

    override fun getStrokeColor(): Int {
        return strokeColor ?: MapConsts.DEFAULT_STROKE_COLOR
    }

    fun getFillColor(): Int {
        val customColor = fillColor?.let {
            ColorUtils.setAlphaComponent(
                it,
                MapConsts.DEFAULT_FILL_COLOR_OPACITY
            )
        }

        return customColor ?: ColorUtils.setAlphaComponent(
            MapConsts.DEFAULT_STROKE_COLOR,
            MapConsts.DEFAULT_FILL_COLOR_OPACITY
        )
    }
}
