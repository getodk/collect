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
        return when {
            basemapSource == ProjectKeys.BASEMAP_SOURCE_GOOGLE -> GoogleMapFragment()
            else -> MapboxClassInstanceCreator.createMapboxMapFragment(
                basemapSource ?: ProjectKeys.BASEMAP_SOURCE_MAPBOX
            )
        }
    }
}
