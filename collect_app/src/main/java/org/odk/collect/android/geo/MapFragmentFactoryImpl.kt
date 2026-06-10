package org.odk.collect.android.geo

import org.odk.collect.android.application.MapboxClassInstanceCreator
import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()
        val basemapSource = settings.getString(KEY_BASEMAP_SOURCE)
        return if (isMapbox(basemapSource)) {
            MapboxClassInstanceCreator.createMapboxMapFragment(basemapSource)
        } else {
            GoogleMapFragment()
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun isMapbox(source: String?): Boolean {
        contract {
            returns(true) implies (source != null)
        }

        return when (source) {
            ProjectKeys.BASEMAP_SOURCE_MAPBOX,
            ProjectKeys.BASEMAP_SOURCE_OSM,
            ProjectKeys.BASEMAP_SOURCE_USGS,
            ProjectKeys.BASEMAP_SOURCE_CARTO -> true

            else -> false
        }
    }
}
