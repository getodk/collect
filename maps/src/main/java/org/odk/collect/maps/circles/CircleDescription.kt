package org.odk.collect.maps.circles

import androidx.core.graphics.ColorUtils
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapPoint

data class CircleDescription(
    val center: MapPoint,
    val radius: Float,
    val color: Int = MapConsts.DEFAULT_STROKE_COLOR
)

fun CircleDescription.getStrokeColor(): Int {
    return color
}

fun CircleDescription.getFillColor(): Int {
    return ColorUtils.setAlphaComponent(color, MapConsts.DEFAULT_FILL_COLOR_OPACITY)
}