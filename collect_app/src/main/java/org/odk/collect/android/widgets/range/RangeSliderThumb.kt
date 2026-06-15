package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun RangeSliderThumb(
    orientation: Orientation,
    modifier: Modifier = Modifier
) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    Box(
        modifier = modifier
            .then(
                if (orientation == Orientation.Horizontal) {
                    Modifier.width(THUMB_THICKNESS).height(THUMB_LENGTH)
                } else {
                    Modifier.width(THUMB_LENGTH).height(THUMB_THICKNESS)
                }
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .semantics { contentDescription = sliderThumbContentDescription }
    )
}

private val THUMB_LENGTH = 40.dp
internal val THUMB_THICKNESS = 6.dp
