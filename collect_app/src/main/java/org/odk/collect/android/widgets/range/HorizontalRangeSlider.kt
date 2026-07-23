package org.odk.collect.android.widgets.range

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HorizontalRangeSlider(
    value: Float?,
    valueLabel: String,
    placeholder: Float?,
    steps: Int,
    ticks: Int,
    enabled: Boolean,
    startLabel: String,
    endLabel: String,
    labels: List<String>,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ValueLabel(valueLabel)

        RangeSliderTrack(
            orientation = Orientation.Horizontal,
            value = value,
            placeholder = placeholder,
            ticks = ticks,
            steps = steps,
            enabled = enabled,
            onValueChanging = onValueChanging,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )

        RangeSliderEdgeLabels(Orientation.Horizontal, startLabel, endLabel)
        RangeSliderStepLabels(Orientation.Horizontal, labels)
    }
}

@Preview
@Composable
private fun HorizontalRangeSliderPreview() {
    Surface {
        HorizontalRangeSlider(
            value = 0.5f,
            valueLabel = "5",
            placeholder = null,
            steps = 9,
            ticks = 11,
            enabled = true,
            startLabel = "0",
            endLabel = "10",
            labels = listOf("very bad", "very good"),
            onValueChanging = {},
            onValueChange = {},
            onValueChangeFinished = {}
        )
    }
}
