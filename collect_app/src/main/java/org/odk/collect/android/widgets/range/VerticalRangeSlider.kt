package org.odk.collect.android.widgets.range

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.odk.collect.androidshared.R.dimen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalRangeSlider(
    sliderState: RangeSliderState,
    onValueChanging: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val sliderContentDescription = stringResource(org.odk.collect.strings.R.string.vertical_slider)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (left, center, right) = createRefs()

            Slider(
                modifier = Modifier
                    .semantics { contentDescription = sliderContentDescription }
                    .constrainAs(center) { centerHorizontallyTo(parent) }
                    .height(330.dp)
                    .pointerInteropFilter { event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            onValueChanging(true)
                            if (sliderState.sliderValue == null) {
                                onValueChange(0f)
                            }
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
                value = sliderState.sliderValue ?: 0f,
                steps = sliderState.numOfSteps,
                onValueChange = onValueChange,
                onValueChangeFinished = {
                    onValueChanging(false)
                    onValueChangeFinished()
                },
                thumb = { Thumb(sliderState.sliderValue) },
                track = { Track(it, sliderState.numOfTicks) },
                enabled = sliderState.isEnabled
            )

            val margin = dimensionResource(id = dimen.margin_standard)

            ValueLabel(
                sliderState.valueLabel,
                modifier = Modifier.constrainAs(left) {
                    end.linkTo(center.start, margin = margin)
                    centerVerticallyTo(center)
                }
            )

            VerticalEdgeLabels(
                sliderState.startLabel,
                sliderState.endLabel,
                modifier = Modifier.constrainAs(right) {
                    start.linkTo(center.end, margin = margin)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = androidx.constraintlayout.compose.Dimension.fillToConstraints
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
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = labelEnd, style = MaterialTheme.typography.headlineSmall)
        Text(text = labelStart, style = MaterialTheme.typography.headlineSmall)
    }
}
