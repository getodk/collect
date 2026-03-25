package org.odk.collect.android.widgets.range

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.odk.collect.androidshared.R.dimen
import kotlin.math.roundToInt

private const val SLIDER_HEIGHT = 330

@OptIn(ExperimentalMaterial3Api::class)
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
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.vertical_slider)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (valueLabelRef, sliderRef, edgeLabelsRef, stepLabelsRef) = createRefs()

            BoxWithConstraints(
                modifier = Modifier
                    .height(SLIDER_HEIGHT.dp)
                    .constrainAs(sliderRef) { centerHorizontallyTo(parent) }
            ) {
                Slider(
                    modifier = Modifier
                        .semantics { contentDescription = sliderContentDescription }
                        .rotateVertically()
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
                                val thumbHeightPx = THUMB_WIDTH.dp.toPx()
                                val trackHeight = constraints.maxHeight - thumbHeightPx
                                val yOffset = trackHeight * (1 - thumbValue)
                                IntOffset(0, yOffset.roundToInt())
                            }
                            .rotateVertically()
                            .pointerInteropFilter { false }
                            .align(Alignment.TopCenter)
                    ) { Thumb(value = thumbValue) }
                }
            }

            val margin = dimensionResource(id = dimen.margin_standard)

            ValueLabel(
                valueLabel,
                modifier = Modifier.constrainAs(valueLabelRef) {
                    end.linkTo(sliderRef.start, margin = margin)
                    centerVerticallyTo(sliderRef)
                }
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
                        centerVerticallyTo(sliderRef)
                    }
            )
        }
    }
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
    BoxWithConstraints(modifier = modifier) {
        val totalSteps = labels.size - 1

        labels.forEachIndexed { index, label ->
            if (label.isBlank()) return@forEachIndexed

            val fraction = if (totalSteps > 0) index.toFloat() / totalSteps else 0f
            var textHeight by mutableIntStateOf(0)

            val modifier = when (index) {
                0 -> Modifier.align(Alignment.BottomStart)
                totalSteps -> Modifier.align(Alignment.TopStart)
                else -> Modifier
                    .align(Alignment.TopStart)
                    .onGloballyPositioned { textHeight = it.size.height }
                    .offset {
                        val yOffset = constraints.maxHeight * (1 - fraction) - textHeight / 2
                        IntOffset(0, yOffset.roundToInt())
                    }
            }

            Label(
                modifier = modifier,
                text = label
            )
        }
    }
}

private fun Modifier.rotateVertically() = this
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
    }
