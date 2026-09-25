package org.odk.collect.android.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var preview by remember(answer) { mutableStateOf<ImageBitmap?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        val height = with(LocalDensity.current) { PREVIEW_HEIGHT.roundToPx() }

        DisposableEffect(answer, width, height) {
            val trace = LineDescription(GeoPolyUtils.parseGeometry(answer))
            val cancel = mediaWidgetAnswerViewModel.renderMapPreview(trace, width, height) {
                preview = it?.asImageBitmap()
            }

            onDispose { cancel() }
        }

        val currentPreview = preview
        if (currentPreview != null) {
            Image(
                bitmap = currentPreview,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .testTag(MAP_PREVIEW_TAG)
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
            )
        } else {
            TextWidgetAnswer(
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

private val PREVIEW_HEIGHT = 200.dp

internal const val MAP_PREVIEW_TAG = "map_preview"
