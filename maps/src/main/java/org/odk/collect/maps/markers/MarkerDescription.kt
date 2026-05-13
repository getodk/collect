package org.odk.collect.maps.markers

import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    val iconAnchor: MapFragment.IconAnchor = MapFragment.IconAnchor.CENTER,
    val iconDescription: MarkerIconDescription
)
