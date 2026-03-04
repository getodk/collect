package org.odk.collect.android.widgets.range

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.math.BigDecimal

@Composable
fun RangeSlider(
    value: BigDecimal?,
    valueLabel: String,
    steps: Int,
    ticks: Int,
    enabled: Boolean,
    valid: Boolean,
    horizontal: Boolean,
    startLabel: String,
    endLabel: String,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onRangeInvalid: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!valid) {
            onRangeInvalid()
        }
    }

    Surface {
        if (horizontal) {
            HorizontalRangeSlider(
                value = value?.toFloat(),
                valueLabel = valueLabel,
                steps = steps,
                ticks = ticks,
                enabled = enabled,
                startLabel = startLabel,
                endLabel = endLabel,
                onValueChanging = onValueChanging,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished
            )
        } else {
            VerticalRangeSlider(
                value = value?.toFloat(),
                valueLabel = valueLabel,
                steps = steps,
                ticks = ticks,
                enabled = enabled,
                startLabel = startLabel,
                endLabel = endLabel,
                onValueChanging = onValueChanging,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished
            )
        }
    }
}
