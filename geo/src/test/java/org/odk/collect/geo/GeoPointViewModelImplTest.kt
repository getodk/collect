package org.odk.collect.geo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker

class GeoPointViewModelImplTest {

    private val locationTracker = mock<LocationTracker>()

    @Test
    fun `starts LocationTracker`() {
        GeoPointViewModelImpl(locationTracker)
        verify(locationTracker).start()
    }

    @Test
    fun `location returns null when no location`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)
        viewModel.accuracyThreshold = 0.0

        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        assertThat(viewModel.location, equalTo(null))
    }

    @Test
    fun `location returns null when accuracy is higher than threshold value`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)
        viewModel.accuracyThreshold = 1.0

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        assertThat(viewModel.location, equalTo(null))
    }

    @Test
    fun `location returns location when accuracy is equal to threshold value`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)
        viewModel.accuracyThreshold = 1.0

        val location = Location(0.0, 0.0, 0.0, 1.0f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(location)
        assertThat(viewModel.location, equalTo(location))
    }

    @Test
    fun `currentAccuracy returns null when no location`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)

        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        assertThat(viewModel.currentAccuracy, equalTo(null))
    }

    @Test
    fun `currentAccuracy updates with location accuracy`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        assertThat(viewModel.currentAccuracy, equalTo(1.1f))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 2.5f))
        assertThat(viewModel.currentAccuracy, equalTo(2.5f))
    }

    @Test
    fun `onCleared() stops LocationTracker`() {
        val viewModel = GeoPointViewModelImpl(locationTracker)
        viewModel.onCleared()
    }
}
