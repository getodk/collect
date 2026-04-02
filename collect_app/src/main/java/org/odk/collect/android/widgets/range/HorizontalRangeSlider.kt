package org.odk.collect.android.widgets.range

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.horizontal_slider)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ValueLabel(valueLabel)

        BoxWithConstraints {
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = sliderContentDescription }
                    .pointerInteropFilter { event ->
                        if (enabled && event.action == MotionEvent.ACTION_DOWN) {
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
                thumb = {},
                track = { Track(it, ticks) },
                enabled = enabled
            )

            val thumbValue = value ?: placeholder
            if (thumbValue != null) {
                Box(
                    modifier = Modifier
                        .offset {
                            val thumbWidthPx = THUMB_WIDTH.dp.toPx()
                            val trackWidth = constraints.maxWidth - thumbWidthPx
                            val xOffset = trackWidth * thumbValue
                            IntOffset(xOffset.roundToInt(), 0)
                        }
                        .pointerInteropFilter { false }
                        .align(Alignment.CenterStart)
                ) { Thumb(value = thumbValue) }
            }
        }

        HorizontalEdgeLabels(startLabel, endLabel)
        HorizontalStepLabels(labels)
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
