package org.odk.collect.geo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.LiveDataTester

class GeoPointViewModelImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val liveDataTester = LiveDataTester()

    private val locationTracker = mock<LocationTracker>()
    private val scheduler = FakeScheduler()

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun `starts LocationTracker`() {
        GeoPointViewModelImpl(locationTracker, scheduler)
        verify(locationTracker).start()
    }

    @Test
    fun `location is null when no location`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.accuracyThreshold = 0.0

        val location = liveDataTester.activate(viewModel.location)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `location is null when accuracy is higher than threshold value`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.accuracyThreshold = 1.0

        val location = liveDataTester.activate(viewModel.location)
        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `location is tracker location when accuracy is equal to threshold value`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.accuracyThreshold = 1.0

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 1.0f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `currentAccuracy is null when no location`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)

        val currentAccuracy = liveDataTester.activate(viewModel.currency)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(null))
    }

    @Test
    fun `currentAccuracy updates with location accuracy`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        val currentAccuracy = liveDataTester.activate(viewModel.currency)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(1.1f))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 2.5f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(2.5f))
    }

    @Test
    fun `onCleared() stops LocationTracker`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.onCleared()
    }

    @Test
    fun `onCleared() cancels repeat`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.onCleared()
        assertThat(scheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun `forceLocation() sets location to location tracker location regardless of threshold`() {
        val viewModel = GeoPointViewModelImpl(locationTracker, scheduler)
        viewModel.accuracyThreshold = 1.0

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 2.5f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))

        viewModel.forceLocation()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }
}
