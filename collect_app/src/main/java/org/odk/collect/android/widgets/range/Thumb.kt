package org.odk.collect.android.widgets.range

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val THUMB_WIDTH = 4

@Composable
fun Thumb(value: Float?) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    if (value != null) {
        SliderDefaults.Thumb(
            modifier = Modifier
                .width(THUMB_WIDTH.dp)
                .semantics { contentDescription = sliderThumbContentDescription },
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

fun Density.calculateThumbOffset(
    trackSize: Int,
    thumbValue: Float,
    isVertical: Boolean
): IntOffset {
    val thumbSizePx = THUMB_WIDTH.dp.toPx()
    val effectiveTrackSize = trackSize - thumbSizePx
    val fraction = if (isVertical) 1 - thumbValue else thumbValue
    val offset = (effectiveTrackSize * fraction).roundToInt()
    return if (isVertical) IntOffset(0, offset) else IntOffset(offset, 0)
}
