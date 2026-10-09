package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
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
    val trace = remember(answer) { LineDescription(GeoPolyUtils.parseGeometry(answer)) }
    val answerToDisplay = GeoWidgetUtils.getGeoPolyAnswerToDisplay(answer)

    AsyncStaticMap(
        modifier = modifier,
        trace = trace,
        renderer = mediaWidgetAnswerViewModel.mapPreviewRenderer,
        contentDescription = answerToDisplay,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        TextWidgetAnswer(Modifier, null, answerToDisplay ?: "", fontSize, horizontalArrangement, onClick, onLongClick)
    }
}
