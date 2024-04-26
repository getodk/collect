package org.odk.collect.maps

import org.odk.collect.androidshared.utils.toColorInt

data class LineDescription(
    val points: List<MapPoint> = emptyList(),
    private val strokeWidth: String? = null,
    private val strokeColor: String? = null,
    val draggable: Boolean = false,
    val closed: Boolean = false
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
}
