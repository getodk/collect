package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.TextWidgetAnswer
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.maplibre.staticmap.StaticMapLoader
import org.odk.collect.maps.traces.TraceDescription

@Composable
fun GeoPolyWidgetAnswer(
    modifier: Modifier,
    answer: String,
    trace: TraceDescription,
    fontSize: Int?,
    horizontalArrangement: Arrangement.Horizontal,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    staticMapLoader: StaticMapLoader,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val answerToDisplay = GeoWidgetUtils.getGeoPolyAnswerToDisplay(answer)

    AsyncStaticMap(
        modifier = modifier,
        trace = trace,
        configuration = mediaWidgetAnswerViewModel.getStaticMapConfiguration(),
        cache = mediaWidgetAnswerViewModel.staticMapCache,
        staticMapLoader = staticMapLoader,
        contentDescription = answerToDisplay,
        onClick = onClick,
        onLongClick = onLongClick,
        failure = {
            TextWidgetAnswer(Modifier, null, answerToDisplay ?: "", fontSize, horizontalArrangement, onClick, onLongClick)
        }
    )
}
