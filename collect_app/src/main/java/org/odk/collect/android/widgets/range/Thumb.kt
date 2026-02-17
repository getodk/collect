package org.odk.collect.android.widgets.range

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun Thumb(value: Float?) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    if (value != null) {
        SliderDefaults.Thumb(
            modifier = Modifier.semantics {
                contentDescription = sliderThumbContentDescription
            },
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}
