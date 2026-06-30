package org.odk.collect.mapbox

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.Test
import org.odk.collect.settings.keys.ProjectKeys

class ConfigurationsTest {
    @Test
    fun cartoStyleUrisUseHttps() {
        val cartoStyleOptions = Configurations.all[ProjectKeys.BASEMAP_SOURCE_CARTO]!!.styleOptions

        val positronUri = cartoStyleOptions["positron"]!!.uri.value
        val darkMatterUri = cartoStyleOptions["dark_matter"]!!.uri.value

        assertThat(positronUri, startsWith("https://"))
        assertThat(darkMatterUri, startsWith("https://"))
        assertThat(positronUri, equalTo("https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"))
        assertThat(darkMatterUri, equalTo("https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"))
    }
}
