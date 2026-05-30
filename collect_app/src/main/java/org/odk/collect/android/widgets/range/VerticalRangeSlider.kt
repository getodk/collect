package org.odk.collect.android.widgets.range

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.odk.collect.androidshared.R.dimen

@Composable
fun VerticalRangeSlider(
    value: Float?,
    placeholder: Float?,
    steps: Int,
    enabled: Boolean,
    valueLabel: String,
    startLabel: String,
    endLabel: String,
    labels: List<String>,
    ticks: Int,
    onValueChanging: (Boolean) -> Unit,
    onValueChangeFinished: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    ConstraintLayout(Modifier.fillMaxWidth()) {
        val (valueLabelRef, sliderRef, edgeLabelsRef, stepLabelsRef) = createRefs()
        val margin = dimensionResource(id = dimen.margin_standard)

        ValueLabel(
            valueLabel,
            modifier = Modifier.constrainAs(valueLabelRef) {
                end.linkTo(sliderRef.start, margin = margin)
                centerVerticallyTo(sliderRef)
            }
        )

        RangeSliderTrack(
            modifier = Modifier
                .height(SLIDER_HEIGHT)
                .constrainAs(sliderRef) { centerHorizontallyTo(parent) },
            orientation = Orientation.Vertical,
            value = value,
            placeholder = placeholder,
            ticks = ticks,
            steps = steps,
            enabled = enabled,
            onValueChanging = onValueChanging,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )

        RangeSliderEdgeLabels(
            orientation = Orientation.Vertical,
            labelStart = startLabel,
            labelEnd = endLabel,
            modifier = Modifier
                .height(SLIDER_HEIGHT)
                .constrainAs(edgeLabelsRef) {
                    start.linkTo(sliderRef.end, margin = margin)
                    centerVerticallyTo(sliderRef)
                }
        )

        RangeSliderStepLabels(
            orientation = Orientation.Vertical,
            labels = labels,
            modifier = Modifier
                .height(SLIDER_HEIGHT)
                .constrainAs(stepLabelsRef) {
                    start.linkTo(edgeLabelsRef.end, margin = margin)
                    end.linkTo(parent.end, margin = margin)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(sliderRef)
                }
        )
    }
}

@Preview
@Composable
private fun VerticalRangeSliderPreview() {
    Surface {
        VerticalRangeSlider(
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

private val SLIDER_HEIGHT = 330.dp
