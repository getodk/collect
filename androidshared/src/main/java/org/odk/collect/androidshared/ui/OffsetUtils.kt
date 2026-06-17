package org.odk.collect.androidshared.ui

import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

object OffsetUtils {
    fun calculateOffset(
        trackSize: Int,
        itemSize: Float,
        value: Float,
        isVertical: Boolean
    ): IntOffset {
        val fraction = if (isVertical) 1 - value else value
        val offset = (trackSize * fraction - itemSize * fraction).roundToInt()
        return if (isVertical) IntOffset(0, offset) else IntOffset(offset, 0)
    }
}
