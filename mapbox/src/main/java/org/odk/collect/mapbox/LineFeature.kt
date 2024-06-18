package org.odk.collect.mapbox

import org.odk.collect.maps.MapPoint

interface LineFeature : MapFeature {
    val points: List<MapPoint>
}
