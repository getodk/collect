package org.odk.collect.mapbox

/**
 * A MapFeature is a physical feature on a map, such as a point, a road,
 * a building, a region, etc.  It is presented to the user as one editable
 * object, though its appearance may be constructed from multiple overlays
 * (e.g. geometric elements, handles for manipulation, etc.).
 */
interface MapFeature {
    /** Removes the feature from the map, leaving it no longer usable.  */
    fun dispose()
}
