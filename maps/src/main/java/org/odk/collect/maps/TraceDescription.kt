package org.odk.collect.maps

import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription.TracePoint

interface TraceDescription {
    val points: List<MapPoint>
    val highlightLastPoint: Boolean
    fun getStrokeWidth(): Float
    fun getStrokeColor(): Int
}

fun TraceDescription.getMarkersForPoints(): List<MarkerDescription> {
    return points.mapIndexed { i, point ->
        val color = if (highlightLastPoint && i == points.lastIndex) {
            MapConsts.DEFAULT_HIGHLIGHT_COLOR
        } else {
            getStrokeColor()
        }

        MarkerDescription(point, true, MapFragment.IconAnchor.CENTER, TracePoint(getStrokeWidth(), color))
    }
}
