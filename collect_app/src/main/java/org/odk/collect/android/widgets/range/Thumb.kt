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
import androidx.compose.ui.unit.dp

const val THUMB_WIDTH = 4

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
