package org.odk.collect.android.widgets.range

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

private const val THUMB_WIDTH = 6

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

        HorizontalTrack(
            value = value,
            placeholder = placeholder,
            ticks = ticks,
            steps = steps,
            enabled = enabled,
            onValueChanging = onValueChanging,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )

        HorizontalEdgeLabels(startLabel, endLabel)
        HorizontalStepLabels(labels)
    }
}

@Composable
private fun HorizontalTrack(
    value: Float?,
    placeholder: Float?,
    ticks: Int,
    steps: Int = 0,
    enabled: Boolean,
    onValueChanging: ((Boolean) -> Unit),
    onValueChange: ((Float) -> Unit),
    onValueChangeFinished: (() -> Unit)
) {
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(steps, layoutDirection) {
                if (enabled) {
                    val trackWidth = size.width.toFloat()
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onValueChanging(true)
                        onValueChange(positionToValue(down.position.x, steps, trackWidth, layoutDirection))

                        do {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull() ?: break
                            if (!pointer.pressed) break
                            pointer.consume()
                            onValueChange(positionToValue(pointer.position.x, steps, trackWidth, layoutDirection))
                        } while (true)

                        onValueChanging(false)
                        onValueChangeFinished()
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.Center)
        ) {
            if (value != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = value)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ticks) { Tick() }
            }
        }

        val thumbValue = value ?: placeholder
        if (thumbValue != null) {
            HorizontalThumb(
                modifier = Modifier
                    .offset {
                        calculateOffset(
                            trackSize = constraints.maxWidth,
                            itemSize = THUMB_WIDTH.dp.toPx(),
                            value = thumbValue,
                            isVertical = false
                        )
                    }
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
private fun HorizontalThumb(modifier: Modifier = Modifier) {
    val sliderThumbContentDescription = stringResource(org.odk.collect.strings.R.string.slider_thumb)

    Box(
        modifier = modifier
            .width(THUMB_WIDTH.dp)
            .height(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .semantics { contentDescription = sliderThumbContentDescription }
    )
}

@Composable
private fun HorizontalEdgeLabels(labelStart: String, labelEnd: String) {
    val sliderStartLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_start_label)
    val sliderEndLabelContentDescription = stringResource(org.odk.collect.strings.R.string.slider_end_label)

    Row(
        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun HorizontalStepLabels(labels: List<String>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val totalSteps = labels.size - 1
        val labelWidth = maxWidth / 5 // Each label takes up a fifth of the track width. Confirmed to look good in most cases.

        labels.forEachIndexed { index, label ->
            if (label.isBlank()) return@forEachIndexed

            val modifier = when (index) {
                0 -> Modifier.align(Alignment.TopStart)
                totalSteps -> Modifier.align(Alignment.TopEnd)
                else -> Modifier.offset {
                    val fraction = index.toFloat() / totalSteps
                    val centerX = (constraints.maxWidth * fraction).roundToInt()
                    IntOffset(centerX - labelWidth.roundToPx() / 2, 0)
                }
            }

            Label(
                modifier = modifier.width(labelWidth),
                text = label,
                textAlign = when (index) {
                    0 -> TextAlign.Start
                    totalSteps -> TextAlign.End
                    else -> TextAlign.Center
                }
            )
        }
    }
}

private fun positionToValue(position: Float, steps: Int, trackWidth: Float, layoutDirection: LayoutDirection): Float {
    val adjustedPosition = if (layoutDirection == LayoutDirection.Rtl) trackWidth - position else position
    val fraction = adjustedPosition.coerceIn(0f, trackWidth) / trackWidth
    val divisions = steps + 1
    return (fraction * divisions).roundToInt().toFloat() / divisions
}
