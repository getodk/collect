package org.odk.collect.maps.markers

import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragment.Companion.CENTER
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.TraceDescription
import org.odk.collect.maps.markers.MarkerIconDescription.TracePoint

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @MapFragment.Companion.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)

fun TraceDescription.getMarkersForPoints(): List<MarkerDescription> {
    return points.mapIndexed { i, point ->
        val color = if (highlightLastPoint && i == points.lastIndex) {
            MapConsts.DEFAULT_HIGHLIGHT_COLOR
        } else {
            getStrokeColor()
        }

        MarkerDescription(point, true, CENTER, TracePoint(getStrokeWidth(), color))
    }
}
