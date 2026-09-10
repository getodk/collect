package org.odk.collect.maplibre

import org.odk.collect.maps.MapPoint

interface LineFeature : MapFeature {
    val points: List<MapPoint>
}
