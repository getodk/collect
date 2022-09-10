package org.odk.collect.maps

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @get:MapFragment.IconAnchor @param:MapFragment.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)
