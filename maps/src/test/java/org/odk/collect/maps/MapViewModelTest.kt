package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.androidtest.recordValues
import org.odk.collect.maps.layers.InMemReferenceLayerRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.InMemSettings

@RunWith(AndroidJUnit4::class)
class MapViewModelTest {

    private val unprotectedSettings = InMemSettings()
    private val metaSettings = InMemSettings()
    private val referenceLayerRepository = InMemReferenceLayerRepository()

    private val viewModel = MapViewModel(unprotectedSettings, metaSettings, referenceLayerRepository)

    @Test
    fun `getSettings delivers current settings on subscription`() {
        val observed = viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).getOrAwaitValue()

        assertThat(observed, equalTo(unprotectedSettings))
    }

    @Test
    fun `getSettings delivers current settings on subscription even after a different key changed`() {
        viewModel.onSettingChanged(ProjectKeys.KEY_REFERENCE_LAYER)

        val observed = viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).getOrAwaitValue()

        assertThat(observed, equalTo(unprotectedSettings))
    }

    @Test
    fun `getSettings re-delivers settings when a requested key changes`() {
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).recordValues { settings ->
            viewModel.onSettingChanged(ProjectKeys.KEY_MAPBOX_MAP_STYLE)

            assertThat(settings.size, equalTo(2))
        }
    }

    @Test
    fun `getSettings does not re-deliver settings when an unrelated key changes`() {
        viewModel.getSettings(setOf(ProjectKeys.KEY_MAPBOX_MAP_STYLE)).recordValues { settings ->
            viewModel.onSettingChanged(ProjectKeys.KEY_REFERENCE_LAYER)

            assertThat(settings.size, equalTo(1))
        }
    }

    @Test
    fun `zoomTo removes duplicate MapPoints`() {
        val point = MapPoint(43.0, 7.0)
        viewModel.zoomTo(
            listOf(point, point),
            0.0,
            false
        )
        assertThat((viewModel.zoom.value as Zoom.Box).box, contains(point))
    }
}
