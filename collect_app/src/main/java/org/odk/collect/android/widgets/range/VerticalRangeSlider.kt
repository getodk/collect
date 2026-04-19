package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.odk.collect.androidshared.R.dimen
import org.odk.collect.androidshared.ui.OffsetUtils.calculateOffset
import kotlin.math.roundToInt

private const val SLIDER_HEIGHT = 330
private const val THUMB_HEIGHT = 6

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

        VerticalTrack(
            modifier = Modifier
                .height(SLIDER_HEIGHT.dp)
                .constrainAs(sliderRef) { centerHorizontallyTo(parent) },
            value = value,
            placeholder = placeholder,
            ticks = ticks,
            steps = steps,
            enabled = enabled,
            onValueChanging = onValueChanging,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )

        VerticalEdgeLabels(
            startLabel,
            endLabel,
            modifier = Modifier
                .height(SLIDER_HEIGHT.dp)
                .constrainAs(edgeLabelsRef) {
                    start.linkTo(sliderRef.end, margin = margin)
                    centerVerticallyTo(sliderRef)
                }
        )

        VerticalStepLabels(
            labels,
            modifier = Modifier
                .height(SLIDER_HEIGHT.dp)
                .constrainAs(stepLabelsRef) {
                    start.linkTo(edgeLabelsRef.end, margin = margin)
                    end.linkTo(parent.end, margin = margin)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(sliderRef)
                }
        )
    }
}

@Composable
private fun VerticalTrack(
    modifier: Modifier = Modifier,
    value: Float?,
    placeholder: Float?,
    ticks: Int,
    steps: Int = 0,
    enabled: Boolean,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.vertical_slider)

    BoxWithConstraints(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .pointerInput(steps) {
                if (enabled) {
                    val trackHeight = size.height.toFloat()
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onValueChanging(true)
                        onValueChange(positionToValue(down.position.y, steps, trackHeight))

                        do {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull() ?: break
                            if (!pointer.pressed) break
                            pointer.consume()
                            onValueChange(positionToValue(pointer.position.y, steps, trackHeight))
                        } while (true)

                        onValueChanging(false)
                        onValueChangeFinished()
                    }
                }
            }
            .semantics { contentDescription = sliderContentDescription }
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.Center)
        ) {
            if (value != null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(fraction = value)
                        .width(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomCenter)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(ticks) { Tick() }
            }
        }

        val thumbValue = value ?: placeholder
        if (thumbValue != null) {
            VerticalThumb(
                modifier = Modifier
                    .offset {
                        calculateOffset(
                            trackSize = constraints.maxHeight,
                            itemSize = THUMB_HEIGHT.dp.toPx(),
                            value = thumbValue,
                            isVertical = true
                        )
                    }
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun VerticalThumb(modifier: Modifier = Modifier) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    Box(
        modifier = modifier
            .width(40.dp)
            .height(THUMB_HEIGHT.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .semantics { contentDescription = sliderThumbContentDescription }
    )
}

@Composable
private fun VerticalEdgeLabels(
    labelStart: String,
    labelEnd: String,
    modifier: Modifier = Modifier
) {
    val sliderStartLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_start_label)
    val sliderEndLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_end_label)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Label(
            modifier = Modifier.semantics {
                contentDescription = sliderEndLabelContentDescription
            },
            text = labelEnd,
        )
        Label(
            modifier = Modifier.semantics {
                contentDescription = sliderStartLabelContentDescription
            },
            text = labelStart,
        )
    }
}

@Composable
private fun VerticalStepLabels(labels: List<String>, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        val totalSteps = labels.size - 1

        SubcomposeLayout { constraints ->
            val placeable = labels.mapIndexed { index, label ->
                if (label.isBlank()) return@mapIndexed null

                val measurables = subcompose(index) {
                    Label(modifier = Modifier, text = label)
                }
                val placeable = measurables.first().measure(constraints)
                val fraction = if (totalSteps > 0) index.toFloat() / totalSteps else 0f

                val y = when (index) {
                    0 -> constraints.maxHeight - placeable.height
                    totalSteps -> 0
                    else -> (constraints.maxHeight * (1 - fraction) - placeable.height / 2).roundToInt()
                }

                index to Triple(placeable, 0, y)
            }.filterNotNull()

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.forEach { (_, triple) ->
                    val (placeable, x, y) = triple
                    placeable.placeRelative(x, y)
                }
            }
        }
    }
}

private fun positionToValue(y: Float, steps: Int, trackHeight: Float): Float {
    val fraction = 1f - y.coerceIn(0f, trackHeight) / trackHeight
    val divisions = steps + 1
    return (fraction * divisions).roundToInt().toFloat() / divisions
}
