package org.odk.collect.android.geo

import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.maplibre.MapLibreMapFragment
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()
        return when (val basemapSource = settings.getString(KEY_BASEMAP_SOURCE)) {
            ProjectKeys.BASEMAP_SOURCE_MAPLIBRE,
            ProjectKeys.BASEMAP_SOURCE_OSM,
            ProjectKeys.BASEMAP_SOURCE_USGS,
            ProjectKeys.BASEMAP_SOURCE_CARTO -> MapLibreMapFragment(basemapSource)
            else -> GoogleMapFragment()
        }
    }
}
