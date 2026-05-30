package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.odk.collect.androidshared.ui.OffsetUtils.calculateOffset
import kotlin.math.roundToInt

@Composable
fun RangeSliderTrack(
    modifier: Modifier = Modifier,
    orientation: Orientation,
    value: Float?,
    placeholder: Float?,
    ticks: Int,
    steps: Int = 0,
    enabled: Boolean,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val sliderContentDescription = stringResource(
        if (orientation == Orientation.Horizontal) org.odk.collect.strings.R.string.horizontal_slider
        else org.odk.collect.strings.R.string.vertical_slider
    )

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (orientation == Orientation.Horizontal) {
                    Modifier.fillMaxWidth().height(INTERACTIVE_SIZE).systemGestureExclusion()
                } else {
                    Modifier.fillMaxHeight().width(INTERACTIVE_SIZE)
                }
            )
            .pointerInput(steps, layoutDirection) {
                if (enabled) {
                    val trackSize = if (orientation == Orientation.Horizontal) size.width.toFloat() else size.height.toFloat()
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onValueChanging(true)
                        onValueChange(
                            positionToValue(
                                if (orientation == Orientation.Horizontal) down.position.x else down.position.y,
                                steps,
                                trackSize,
                                orientation,
                                layoutDirection
                            )
                        )

                        do {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull() ?: break
                            if (!pointer.pressed) break
                            pointer.consume()
                            onValueChange(
                                positionToValue(
                                    if (orientation == Orientation.Horizontal) pointer.position.x else pointer.position.y,
                                    steps,
                                    trackSize,
                                    orientation,
                                    layoutDirection
                                )
                            )
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
                .then(
                    if (orientation == Orientation.Horizontal) {
                        Modifier.fillMaxWidth().height(TRACK_THICKNESS)
                    } else {
                        Modifier.fillMaxHeight().width(TRACK_THICKNESS)
                    }
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.Center)
        ) {
            if (value != null) {
                Box(
                    modifier = Modifier
                        .then(
                            if (orientation == Orientation.Horizontal) {
                                Modifier.fillMaxWidth(value).height(TRACK_THICKNESS)
                            } else {
                                Modifier.fillMaxHeight(value).width(TRACK_THICKNESS)
                            }
                        )
                        .background(MaterialTheme.colorScheme.primary)
                        .then(
                            if (orientation == Orientation.Vertical) Modifier.align(Alignment.BottomCenter)
                            else Modifier
                        )
                )
            }

            if (orientation == Orientation.Horizontal) {
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(ticks) { index ->
                        Tick(isEdgeTick = index == 0 || index == ticks - 1)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxHeight().align(Alignment.Center),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(ticks) { index ->
                        Tick(isEdgeTick = index == 0 || index == ticks - 1)
                    }
                }
            }
        }

        val thumbValue = value ?: placeholder
        if (thumbValue != null) {
            RangeSliderThumb(
                orientation = orientation,
                modifier = Modifier
                    .offset {
                        calculateOffset(
                            trackSize = if (orientation == Orientation.Horizontal) constraints.maxWidth else constraints.maxHeight,
                            itemSize = THUMB_THICKNESS.toPx(),
                            value = thumbValue,
                            isVertical = orientation == Orientation.Vertical
                        )
                    }
                    .align(if (orientation == Orientation.Horizontal) Alignment.CenterStart else Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun RangeSliderThumb(
    orientation: Orientation,
    modifier: Modifier = Modifier
) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    Box(
        modifier = modifier
            .then(
                if (orientation == Orientation.Horizontal) {
                    Modifier.width(THUMB_THICKNESS).height(THUMB_LENGTH)
                } else {
                    Modifier.width(THUMB_LENGTH).height(THUMB_THICKNESS)
                }
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .semantics { contentDescription = sliderThumbContentDescription }
    )
}

@Composable
fun RangeSliderEdgeLabels(
    orientation: Orientation,
    labelStart: String,
    labelEnd: String,
    modifier: Modifier = Modifier
) {
    val sliderStartLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_start_label)
    val sliderEndLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_end_label)

    if (orientation == Orientation.Horizontal) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Label(
                modifier = Modifier.semantics {
                    contentDescription = sliderStartLabelContentDescription
                },
                text = labelStart,
            )
            Label(
                modifier = Modifier.semantics {
                    contentDescription = sliderEndLabelContentDescription
                },
                text = labelEnd,
            )
        }
    } else {
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
}

@Composable
fun RangeSliderStepLabels(
    orientation: Orientation,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val totalSteps = labels.size - 1

    if (orientation == Orientation.Horizontal) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val labelWidth = maxWidth / 5 // Each label takes up a fifth of the track width. Confirmed to look good in most cases.

            labels.forEachIndexed { index, label ->
                if (label.isBlank()) return@forEachIndexed

                val labelModifier = when (index) {
                    0 -> Modifier.align(Alignment.TopStart)
                    totalSteps -> Modifier.align(Alignment.TopEnd)
                    else -> Modifier.offset {
                        val fraction = index.toFloat() / totalSteps
                        val centerX = (constraints.maxWidth * fraction).roundToInt()
                        IntOffset(centerX - labelWidth.roundToPx() / 2, 0)
                    }
                }

                Label(
                    modifier = labelModifier.width(labelWidth),
                    text = label,
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        totalSteps -> TextAlign.End
                        else -> TextAlign.Center
                    }
                )
            }
        }
    } else {
        Box(modifier = modifier) {
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
}

private fun positionToValue(
    position: Float,
    steps: Int,
    trackSize: Float,
    orientation: Orientation,
    layoutDirection: LayoutDirection
): Float {
    val fraction = if (orientation == Orientation.Horizontal) {
        val adjustedPosition = if (layoutDirection == LayoutDirection.Rtl) trackSize - position else position
        adjustedPosition.coerceIn(0f, trackSize) / trackSize
    } else {
        1f - position.coerceIn(0f, trackSize) / trackSize
    }
    val divisions = steps + 1
    return (fraction * divisions).roundToInt().toFloat() / divisions
}

private val TRACK_THICKNESS = 20.dp
private val THUMB_LENGTH = 40.dp
private val THUMB_THICKNESS = 6.dp
private val INTERACTIVE_SIZE = 48.dp
