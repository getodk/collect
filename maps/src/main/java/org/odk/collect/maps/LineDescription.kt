package org.odk.collect.maps

import org.odk.collect.androidshared.utils.toColorInt

data class LineDescription(
    val points: List<MapPoint>,
    private val strokeWidth: String?,
    private val strokeColor: String?,
    val draggable: Boolean,
    val closed: Boolean
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
        } catch (e: Throwable) {
            MapConsts.DEFAULT_STROKE_WIDTH
        }
    }

    fun getStrokeColor(): Int {
        val customColor = strokeColor?.toColorInt()
        return customColor ?: MapConsts.DEFAULT_STROKE_COLOR.toColorInt()!!
    }
}
