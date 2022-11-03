package org.odk.collect.android.geo

import org.odk.collect.android.application.MapboxClassInstanceCreator
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.osmdroid.OsmDroidMapFragment
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_STAMEN
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()

        return when {
            isBasemapOSM(settings.getString(KEY_BASEMAP_SOURCE)) -> OsmDroidMapFragment()
            settings.getString(KEY_BASEMAP_SOURCE) == BASEMAP_SOURCE_MAPBOX -> MapboxClassInstanceCreator.createMapboxMapFragment() ?: GoogleMapFragment()
            else -> GoogleMapFragment()
        }
    }

    private fun isBasemapOSM(basemap: String?): Boolean {
        return basemap == BASEMAP_SOURCE_OSM ||
            basemap == BASEMAP_SOURCE_USGS ||
            basemap == BASEMAP_SOURCE_CARTO ||
            basemap == BASEMAP_SOURCE_STAMEN
    }
}
