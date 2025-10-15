package org.odk.collect.maps

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.shared.settings.InMemSettings

@RunWith(AndroidJUnit4::class)
class MapViewModelTest {
    @Test
    fun `zoomTo removes duplicate MapPoints`() {
        val viewModel = MapViewModel(InMemSettings(), InMemSettings())
        val point = MapPoint(43.0, 7.0)
        viewModel.zoomTo(
            listOf(point, point),
            0.0,
            false
        )
        assertThat((viewModel.zoom.value as Zoom.Box).box, contains(point))
    }
}
