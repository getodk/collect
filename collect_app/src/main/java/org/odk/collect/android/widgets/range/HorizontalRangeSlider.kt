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
    value: Float?,
    valueLabel: String,
    steps: Int,
    ticks: Int,
    enabled: Boolean,
    startLabel: String,
    endLabel: String,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.horizontal_slider)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ValueLabel(valueLabel)

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = sliderContentDescription }
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onValueChanging(true)
                        if (value == null) {
                            onValueChange(0f)
                        }
                    }
                    false
                },
            value = value ?: 0f,
            steps = steps,
            onValueChange = onValueChange,
            onValueChangeFinished = {
                onValueChanging(false)
                onValueChangeFinished()
            },
            thumb = { Thumb(value) },
            track = { Track(it, ticks) },
            enabled = enabled
        )

        HorizontalEdgeLabels(startLabel, endLabel)
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
