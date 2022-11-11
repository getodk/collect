package org.odk.collect.android.geo

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.osmdroid.OsmDroidMapFragment
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class MapFragmentFactoryImplTest {

    private val settingsProvider = InMemSettingsProvider()
    private val mapFragmentFactoryImpl = MapFragmentFactoryImpl(settingsProvider)

    @Test
    fun `OsmDroidMapFragment should be return if any of OSM options selected in settings`() {
        // BASEMAP_SOURCE_OSM
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_OSM)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(OsmDroidMapFragment::class.java)
        )

        // BASEMAP_SOURCE_USGS
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_USGS)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(OsmDroidMapFragment::class.java)
        )

        // BASEMAP_SOURCE_CARTO
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_CARTO)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(OsmDroidMapFragment::class.java)
        )

        // BASEMAP_SOURCE_STAMEN
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_STAMEN)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(OsmDroidMapFragment::class.java)
        )
    }

    @Test
    fun `GoogleMapFragment should be return if Google Maps selected in settings`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_GOOGLE)

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(GoogleMapFragment::class.java)
        )
    }

    @Test
    fun `GoogleMapFragment should be return if corresponding value stored in settings is unsupported`() {
        settingsProvider
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, "Blah")

        assertThat(
            mapFragmentFactoryImpl.createMapFragment(),
            instanceOf(GoogleMapFragment::class.java)
        )
    }
}
