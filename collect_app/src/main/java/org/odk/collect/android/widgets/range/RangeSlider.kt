package org.odk.collect.android.widgets.range

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.ToastUtils

@Composable
fun RangeSlider(
    rangeSliderState: RangeSliderState,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val invalidRangeMsg = stringResource(org.odk.collect.strings.R.string.invalid_range_widget)

    LaunchedEffect(Unit) {
        if (!rangeSliderState.isValid) {
            ToastUtils.showLongToast(invalidRangeMsg)
        }
    }

    if (rangeSliderState.isHorizontal) {
        HorizontalRangeSlider(
            rangeSliderState,
            interactionSource,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
    } else {
        VerticalRangeSlider(
            rangeSliderState,
            interactionSource,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizontalRangeSlider(
    rangeSliderState: RangeSliderState,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.horizontal_slider)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ValueLabel(rangeSliderState.valueLabel)

        Slider(
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = sliderContentDescription
            },
            value = rangeSliderState.sliderValue ?: 0f,
            steps = rangeSliderState.numOfSteps,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            thumb = { Thumb(rangeSliderState.sliderValue, interactionSource) },
            track = { Track(it, rangeSliderState.numOfTicks) },
            interactionSource = interactionSource,
            enabled = rangeSliderState.isEnabled
        )

        HorizontalEdgeLabels(
            rangeSliderState.startLabel,
            rangeSliderState.endLabel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerticalRangeSlider(
    rangeSliderState: RangeSliderState,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val view = LocalView.current
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.vertical_slider)

    ConstraintLayout(Modifier.fillMaxWidth()) {
        val (left, center, right) = createRefs()

        Slider(
            modifier = Modifier
                .semantics {
                    contentDescription = sliderContentDescription
                }
                .constrainAs(center) {
                    centerHorizontallyTo(parent)
                }
                .height(330.dp)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE ->
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL ->
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = constraints.minHeight,
                            maxWidth = constraints.maxHeight,
                            minHeight = constraints.minWidth,
                            maxHeight = constraints.maxHeight,
                        )
                    )
                    layout(placeable.height, placeable.width) {
                        placeable.place(-placeable.width, 0)
                    }
                },
            value = rangeSliderState.sliderValue ?: 0f,
            steps = rangeSliderState.numOfSteps,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            thumb = { Thumb(rangeSliderState.sliderValue, interactionSource) },
            track = { Track(it, rangeSliderState.numOfTicks) },
            interactionSource = interactionSource,
            enabled = rangeSliderState.isEnabled
        )

        val margin = dimensionResource(id = dimen.margin_standard)

        ValueLabel(
            rangeSliderState.valueLabel,
            modifier = Modifier.constrainAs(left) {
                end.linkTo(center.start, margin = margin)
                centerVerticallyTo(center)
            }
        )

        VerticalEdgeLabels(
            rangeSliderState.startLabel,
            rangeSliderState.endLabel,
            modifier = Modifier.constrainAs(right) {
                start.linkTo(center.end, margin = margin)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                height = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )
    }
}

@Composable
private fun ValueLabel(
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
private fun Track(sliderState: SliderState, ticks: Int) {
    val sliderTickContentDescription = stringResource(org.odk.collect.strings.R.string.slider_tick)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
private fun Thumb(value: Float?, interactionSource: MutableInteractionSource) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    if (value != null) {
        SliderDefaults.Thumb(
            modifier = Modifier.semantics {
                contentDescription = sliderThumbContentDescription
            },
            interactionSource = interactionSource
        )
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

@Composable
private fun VerticalEdgeLabels(
    labelStart: String,
    labelEnd: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = labelEnd, style = MaterialTheme.typography.headlineSmall)
        Text(text = labelStart, style = MaterialTheme.typography.headlineSmall)
    }
}
