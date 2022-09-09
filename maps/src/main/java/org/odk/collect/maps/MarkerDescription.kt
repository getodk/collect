package org.odk.collect.maps

data class MarkerDescription(
    val point: MapPoint,
    val isDraggable: Boolean,
    @get:MapFragment.IconAnchor @param:MapFragment.IconAnchor val iconAnchor: String,
    val iconDescription: MarkerIconDescription
)

data class MarkerIconDescription(
    val iconDrawableId: Int,
    val color: String? = null,
    val symbol: String? = null
)
