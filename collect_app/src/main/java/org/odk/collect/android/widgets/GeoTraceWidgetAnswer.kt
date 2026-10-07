package org.odk.collect.android.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.geo.geopoly.GeoPolyUtils
import org.odk.collect.maps.traces.LineDescription

@Composable
fun GeoTraceWidgetAnswer(
    modifier: Modifier,
    answer: String,
    fontSize: Int?,
    horizontalArrangement: Arrangement.Horizontal,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var preview by remember(answer) { mutableStateOf<MapPreviewState>(MapPreviewState.Loading) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        val height = with(LocalDensity.current) { PREVIEW_HEIGHT.roundToPx() }

        DisposableEffect(answer, width, height) {
            val trace = LineDescription(GeoPolyUtils.parseGeometry(answer))
            val cancel = if (trace.points.all { GeoWidgetUtils.isWithinMapBounds(it) }) {
                mediaWidgetAnswerViewModel.renderMapPreview(trace, width, height) {
                    preview = if (it != null) MapPreviewState.Loaded(it.asImageBitmap()) else MapPreviewState.Failed
                }
            } else {
                preview = MapPreviewState.Failed
                {}
            }

            onDispose { cancel() }
        }

        val previewModifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_HEIGHT)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (MultiClickGuard.allowClick()) {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            )

        when (val currentPreview = preview) {
            MapPreviewState.Loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = previewModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                CircularProgressIndicator()
            }

            is MapPreviewState.Loaded -> Image(
                bitmap = currentPreview.bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = previewModifier.testTag(MAP_PREVIEW_TAG)
            )

            MapPreviewState.Failed -> TextWidgetAnswer(
                Modifier,
                null,
                GeoWidgetUtils.getGeoPolyAnswerToDisplay(answer) ?: "",
                fontSize,
                horizontalArrangement,
                onClick,
                onLongClick
            )
        }
    }
}

private sealed interface MapPreviewState {
    data object Loading : MapPreviewState
    data class Loaded(val bitmap: ImageBitmap) : MapPreviewState
    data object Failed : MapPreviewState
}

private val PREVIEW_HEIGHT = 200.dp

internal const val MAP_PREVIEW_TAG = "map_preview"
