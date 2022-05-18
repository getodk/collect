package org.odk.collect.android.geo

import android.content.Context
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.osmdroid.OsmDroidMapFragment
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_STAMEN
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import org.odk.collect.shared.settings.Settings

class MapFragmentFactoryImpl(private val settings: Settings) : MapFragmentFactory {

    override fun createMapFragment(context: Context): MapFragment {
        return if (isBasemapOSM(settings.getString(KEY_BASEMAP_SOURCE))) {
            OsmDroidMapFragment()
        } else if (settings.getString(KEY_BASEMAP_SOURCE) == BASEMAP_SOURCE_MAPBOX) {
            MapboxMapFragment()
        } else {
            GoogleMapFragment()
        }
    }

    private fun isBasemapOSM(basemap: String?): Boolean {
        return basemap == BASEMAP_SOURCE_OSM ||
            basemap == BASEMAP_SOURCE_USGS ||
            basemap == BASEMAP_SOURCE_CARTO ||
            basemap == BASEMAP_SOURCE_STAMEN
    }
}
