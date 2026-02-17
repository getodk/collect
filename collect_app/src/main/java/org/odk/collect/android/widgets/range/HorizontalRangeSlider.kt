package org.odk.collect.android.widgets.range

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalRangeSlider(
    sliderState: RangeSliderState,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.horizontal_slider)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ValueLabel(sliderState.valueLabel)

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = sliderContentDescription }
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onValueChanging(true)
                        if (sliderState.sliderValue == null) {
                            onValueChange(0f)
                        }
                    }
                    false
                },
            value = sliderState.sliderValue ?: 0f,
            steps = sliderState.numOfSteps,
            onValueChange = onValueChange,
            onValueChangeFinished = {
                onValueChanging(false)
                onValueChangeFinished()
            },
            thumb = { Thumb(sliderState.sliderValue) },
            track = { Track(it, sliderState.numOfTicks) },
            enabled = sliderState.isEnabled
        )

        HorizontalEdgeLabels(sliderState.startLabel, sliderState.endLabel)
    }
}

@Composable
private fun HorizontalEdgeLabels(labelStart: String, labelEnd: String) {
    val sliderStartLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_start_label)
    val sliderEndLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_end_label)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = labelStart,
            modifier = Modifier.semantics {
                contentDescription = sliderStartLabelContentDescription
            },
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = labelEnd,
            modifier = Modifier.semantics {
                contentDescription = sliderEndLabelContentDescription
            },
            style = MaterialTheme.typography.titleLarge
        )
    }
}
