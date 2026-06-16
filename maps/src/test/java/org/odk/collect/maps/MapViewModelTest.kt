package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.maps.layers.InMemReferenceLayerRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class MapViewModelTest {

    private val unprotectedSettings = InMemSettings()
    private val metaSettings = InMemSettings()
    private val referenceLayerRepository = InMemReferenceLayerRepository()

    private val viewModel = MapViewModel(unprotectedSettings, metaSettings, referenceLayerRepository)

    @Test
    fun `getSettings delivers current settings on subscription`() {
        var observed: Settings? = null
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).observeForever {
            observed = it
        }

        assertThat(observed, equalTo(unprotectedSettings))
    }

    @Test
    fun `getSettings delivers current settings on subscription even after a different key changed`() {
        viewModel.onSettingChanged(ProjectKeys.KEY_REFERENCE_LAYER)

        var observed: Settings? = null
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).observeForever {
            observed = it
        }

        assertThat(observed, equalTo(unprotectedSettings))
    }

    @Test
    fun `getSettings re-delivers settings when a requested key changes`() {
        var deliveries = 0
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).observeForever {
            deliveries++
        }

        viewModel.onSettingChanged(ProjectKeys.KEY_MAPBOX_MAP_STYLE)

        assertThat(deliveries, equalTo(2))
    }

    @Test
    fun `getSettings does not re-deliver settings when an unrelated key changes`() {
        var deliveries = 0
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).observeForever {
            deliveries++
        }

        viewModel.onSettingChanged(ProjectKeys.KEY_REFERENCE_LAYER)

        assertThat(deliveries, equalTo(1))
    }
}
