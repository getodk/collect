package org.odk.collect.android.widgets.geo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.odk.collect.android.widgets.MediaWidgetAnswerViewModel
import org.odk.collect.android.widgets.TextWidgetAnswer
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.geo.geopoly.GeoPolyUtils
import org.odk.collect.maplibre.staticmap.StaticMapLoader
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.traces.PolygonDescription

@Composable
fun GeoPolyWidgetAnswer(
    modifier: Modifier,
    answer: String,
    isPolygon: Boolean,
    fontSize: Int?,
    horizontalArrangement: Arrangement.Horizontal,
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    staticMapLoader: StaticMapLoader,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val answerToDisplay = GeoWidgetUtils.getGeoPolyAnswerToDisplay(answer)
    val trace = remember(answer, isPolygon) {
        val points = GeoPolyUtils.parseGeometry(answer)
        if (isPolygon) PolygonDescription(points) else LineDescription(points)
    }

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
