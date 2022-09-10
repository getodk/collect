package org.odk.collect.maps.markers

import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @get:MapFragment.IconAnchor @param:MapFragment.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)
