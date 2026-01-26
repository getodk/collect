package org.odk.collect.maps

interface TraceDescription {
    val points: List<MapPoint>
    val highlightLastPoint: Boolean
    fun getStrokeWidth(): Float
    fun getStrokeColor(): Int
}
