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

class LocationTrackerGeoPointViewModelTest {

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
    fun `start() starts LocationTracker with false retain mock accuracy and 1s update interval`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()

        verify(locationTracker).start(retainMockAccuracy = false, updateInterval = 1000L)
    }

    @Test
    fun `start() starts LocationTracker with with retain mock accuracy value when set`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(retainMockAccuracy = true)

        verify(locationTracker).start(true, 1000L)
    }

    @Test
    fun `location is null when no location`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(accuracyThreshold = 0.0f)

        val location = liveDataTester.activate(viewModel.location)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `location is null when accuracy is higher than threshold value`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(accuracyThreshold = 1.0f)

        val location = liveDataTester.activate(viewModel.location)
        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `location is tracker location when accuracy is equal to threshold value`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(accuracyThreshold = 1.0f)

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 1.0f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `location does not update after it has met the threshold`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(accuracyThreshold = 1.0f)

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 1.0f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(1.0, 1.0, 1.0, 1.0f))
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `currentAccuracy is null when no location`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(null))
    }

    @Test
    fun `currentAccuracy updates with location accuracy`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(1.1f))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 2.5f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(2.5f))
    }

    @Test
    fun `onCleared() stops LocationTracker`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()
        viewModel.onCleared()
    }

    @Test
    fun `onCleared() cancels repeat`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()
        viewModel.onCleared()
        assertThat(scheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun `forceLocation() sets location to location tracker location regardless of threshold`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start(accuracyThreshold = 1.0f)

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 2.5f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))

        viewModel.forceLocation()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    /**
     * We want to avoid timing issues where an update happens right after a click and the user
     * ends up with a location fix that was never on screen.
     */
    @Test
    fun `forceLocation() locks location to current one`() {
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { 0 }, scheduler)
        viewModel.start()

        val location = liveDataTester.activate(viewModel.location)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 2.5f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        viewModel.forceLocation()
        assertThat(location.value, equalTo(locationTrackerLocation))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 5.5f))
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `timeElapsed updates with time since creation`() {
        var timeElapsed = 0L
        val viewModel = LocationTrackerGeoPointViewModel(locationTracker, { timeElapsed }, scheduler)
        viewModel.start()

        val timeElapsedLiveData = liveDataTester.activate(viewModel.timeElapsed)

        timeElapsed = 5L
        scheduler.runForeground()
        assertThat(timeElapsedLiveData.value, equalTo(5L))

        timeElapsed = 60L
        scheduler.runForeground()
        assertThat(timeElapsedLiveData.value, equalTo(60L))
    }
}
