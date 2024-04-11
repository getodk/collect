package org.odk.collect.maps

import androidx.core.graphics.ColorUtils
import org.odk.collect.androidshared.utils.toColorInt

data class PolygonDescription(
    val points: List<MapPoint>,
    private val strokeColor: String?,
    private val fillColor: String?
) {
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
