package org.odk.collect.android.geo

import android.content.Context
import org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_GOOGLE
import org.odk.collect.android.preferences.GeneralKeys.BASEMAP_SOURCE_MAPBOX

/**
 * For the map tab we are constrained to use either [BASEMAP_SOURCE_GOOGLE] or [BASEMAP_SOURCE_MAPBOX].
 * This restriction must be kept because for other map sources custom markers are not yet implemented.
 * See: [MapFragment.addMarker]
 */
class MapTabMapProvider : MapProvider() {

    override fun createMapFragment(context: Context): MapFragment? {
        return when (val mapFragment = super.createMapFragment(context)) {
            is MapboxMapFragment,
            is GoogleMapFragment -> mapFragment
            else -> getConfigurator(BASEMAP_SOURCE_GOOGLE).createMapFragment(context)
        }
    }

}