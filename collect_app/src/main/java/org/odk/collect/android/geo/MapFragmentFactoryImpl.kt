package org.odk.collect.android.geo

import org.odk.collect.android.application.MapboxClassInstanceCreator
import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()
        val basemapSource = settings.getString(KEY_BASEMAP_SOURCE)
        return if (isMapbox(basemapSource)) {
            MapboxClassInstanceCreator.createMapboxMapFragment(
                basemapSource ?: ProjectKeys.BASEMAP_SOURCE_MAPBOX
            )
        } else {
            GoogleMapFragment()
        }
    }

    private fun isMapbox(source: String?): Boolean {
        return when (source) {
            ProjectKeys.BASEMAP_SOURCE_MAPBOX,
            ProjectKeys.BASEMAP_SOURCE_OSM,
            ProjectKeys.BASEMAP_SOURCE_USGS,
            ProjectKeys.BASEMAP_SOURCE_CARTO -> true

            else -> false
        }
    }
}
