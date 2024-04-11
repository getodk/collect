package org.odk.collect.maps

import org.odk.collect.androidshared.utils.toColorInt

data class LineDescription(
    val points: List<MapPoint>,
    private val strokeColor: String?,
    val draggable: Boolean,
    val closed: Boolean
) {
    fun getStrokeColor(): Int {
        val customColor = strokeColor?.toColorInt()
        return customColor ?: "#ffff0000".toColorInt()!!
    }
}
