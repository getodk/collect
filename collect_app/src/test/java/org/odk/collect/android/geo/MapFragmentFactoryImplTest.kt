package org.odk.collect.android.geo

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.maplibre.MapLibreMapFragment
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class MapFragmentFactoryImplTest {

    private val settingsProvider = InMemSettingsProvider()
    private val mapFragmentFactoryImpl = MapFragmentFactoryImpl(settingsProvider)

    @Test
    fun `GoogleMapFragment should be returned if Google Maps selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_GOOGLE)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(GoogleMapFragment::class.java)
        )
    }

    @Test
    fun `GoogleMapFragment should be returned if corresponding value stored in settings is unsupported`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, "Blah")

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(GoogleMapFragment::class.java)
        )
    }

    @Test
    fun `MapLibreMapFragment should be returned if MapLibre selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_MAPLIBRE)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(MapLibreMapFragment::class.java)
        )
    }

    @Test
    fun `MapLibreMapFragment should be returned if OpenStreetMap selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_OSM)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(MapLibreMapFragment::class.java)
        )
    }

    @Test
    fun `MapLibreMapFragment should be returned if USGS selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_USGS)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(MapLibreMapFragment::class.java)
        )
    }

    @Test
    fun `MapLibreMapFragment should be returned if Carto selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_CARTO)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(MapLibreMapFragment::class.java)
        )
    }
}
