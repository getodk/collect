package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun ValueLabel(
    value: String,
    modifier: Modifier = Modifier
) {
    val currentSliderValueContentDescription = stringResource(org.odk.collect.strings.R.string.current_slider_value)

    Text(
        text = value,
        modifier = modifier.semantics {
            contentDescription = currentSliderValueContentDescription
        },
        style = MaterialTheme.typography.headlineSmall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Track(sliderState: SliderState, ticks: Int) {
    val sliderTickContentDescription = stringResource(org.odk.collect.strings.R.string.slider_tick)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(
                    fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                )
                .height(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(ticks) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .semantics {
                            contentDescription = sliderTickContentDescription
                        }
                )
            }
        }
    }
}

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
