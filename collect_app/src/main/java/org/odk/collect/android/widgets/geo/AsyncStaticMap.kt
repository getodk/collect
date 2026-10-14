package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import org.odk.collect.androidshared.ui.multiclicksafe.multiClickSafe
import org.odk.collect.maplibre.staticmap.StaticMapCache
import org.odk.collect.maplibre.staticmap.StaticMapConfiguration
import org.odk.collect.maplibre.staticmap.StaticMapLoader
import org.odk.collect.maps.traces.TraceDescription

@Composable
fun AsyncStaticMap(
    modifier: Modifier,
    trace: TraceDescription,
    configuration: StaticMapConfiguration,
    cache: StaticMapCache,
    staticMapLoader: StaticMapLoader,
    contentDescription: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    failure: @Composable () -> Unit
) {
    var state by remember(trace, configuration) { mutableStateOf<StaticMapState>(StaticMapState.Loading) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        val height = with(LocalDensity.current) { STATIC_MAP_HEIGHT.roundToPx() }

        DisposableEffect(trace, configuration, width, height) {
            val cancel = if (trace.points.all { GeoWidgetUtils.isWithinMapBounds(it) }) {
                staticMapLoader.load(configuration, cache, trace, width, height) {
                    state = if (it != null) StaticMapState.Loaded(it.asImageBitmap()) else StaticMapState.Failed
                }
            } else {
                state = StaticMapState.Failed
                {}
            }

            onDispose { cancel() }
        }

        val mapModifier = Modifier
            .fillMaxWidth()
            .height(STATIC_MAP_HEIGHT)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = multiClickSafe(onClick),
                onLongClick = onLongClick
            )

        when (val currentState = state) {
            StaticMapState.Loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = mapModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                CircularProgressIndicator()
            }

            is StaticMapState.Loaded -> Image(
                bitmap = currentState.bitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = mapModifier.testTag(StaticMapSemantics.TAG)
            )

            StaticMapState.Failed -> failure()
        }
    }
}

private sealed interface StaticMapState {
    data object Loading : StaticMapState
    data class Loaded(val bitmap: ImageBitmap) : StaticMapState
    data object Failed : StaticMapState
}

private val STATIC_MAP_HEIGHT = 200.dp

internal object StaticMapSemantics {
    const val TAG = "StaticMap"
}
