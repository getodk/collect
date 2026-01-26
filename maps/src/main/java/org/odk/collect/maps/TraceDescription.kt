package org.odk.collect.maps

interface TraceDescription {
    val highlightLastPoint: Boolean
    fun getStrokeWidth(): Float
    fun getStrokeColor(): Int
}
