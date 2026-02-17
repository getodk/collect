package org.odk.collect.android.widgets.range

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RangeSliderFactory(
    initialState: RangeSliderState,
    onValueChanging: (Boolean) -> Unit,
    onValueChangeFinished: (RangeSliderState) -> Unit,
    onRangeInvalid: () -> Unit
) {
    var sliderState by remember(initialState.sliderValue) { mutableStateOf(initialState) }

    LaunchedEffect(Unit) {
        if (!sliderState.isValid) {
            onRangeInvalid()
        }
    }

    Surface {
        if (sliderState.isHorizontal) {
            HorizontalRangeSlider(
                sliderState = sliderState,
                onValueChanging = onValueChanging,
                onValueChange = { newValue ->
                    sliderState = sliderState.copy(sliderValue = newValue)
                },
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState)
                }
            )
        } else {
            VerticalRangeSlider(
                sliderState = sliderState,
                onValueChanging = onValueChanging,
                onValueChange = { newValue ->
                    sliderState = sliderState.copy(sliderValue = newValue)
                },
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState)
                }
            )
        }
    }
}
