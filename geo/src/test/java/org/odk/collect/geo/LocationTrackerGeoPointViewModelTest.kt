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
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.location.Location
import org.odk.collect.location.satellites.SatelliteInfoClient
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.LiveDataTester

class LocationTrackerGeoPointViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val liveDataTester = LiveDataTester()

    private val locationTracker = mock<LocationTracker>()
    private val satelliteInfoClient = mock<SatelliteInfoClient>()
    private val scheduler = FakeScheduler()

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun `start() starts LocationTracker with false retain mock accuracy and 1s update interval`() {
        val viewModel = createViewModel()
        viewModel.start()

        verify(locationTracker).start(retainMockAccuracy = false, updateInterval = 1000L)
    }

    @Test
    fun `start() starts LocationTracker with with retain mock accuracy value when set`() {
        val viewModel = createViewModel()
        viewModel.start(retainMockAccuracy = true,)

        verify(locationTracker).start(true, 1000L)
    }

    @Test
    fun `acceptedLocation is null when no location`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 0.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `acceptedLocation is null when accuracy is higher than threshold value`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 1.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 1.1f))
        scheduler.runForeground()
        assertThat(location.value, equalTo(null))
    }

    @Test
    fun `acceptedLocation is tracker location when accuracy is equal to threshold value`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 1.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 1.0f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `acceptedLocation is tracker location when accuracy is lower than threshold value`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 1.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
        val locationTrackerLocation = Location(0.0, 0.0, 0.0, 0.9f)
        whenever(locationTracker.getCurrentLocation()).thenReturn(locationTrackerLocation)
        scheduler.runForeground()
        assertThat(location.value, equalTo(locationTrackerLocation))
    }

    @Test
    fun `acceptedLocation does not update after it has met the threshold`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 1.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
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
        val viewModel = createViewModel()
        viewModel.start()

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)
        whenever(locationTracker.getCurrentLocation()).thenReturn(null)
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(null))
    }

    @Test
    fun `currentAccuracy updates with location accuracy`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 5.0f, unacceptableAccuracyThreshold = 20f)

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 6.1f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Improving(6.1f)))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 5.0f + 5.1f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Poor(5.0f + 5.1f)))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 20.1f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Unacceptable(20.1f)))
    }

    @Test
    fun `currentAccuracy is never Poor when unacceptableAccuracyThreshold is equal to accuracyThreshold + 5`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 5f, unacceptableAccuracyThreshold = 10f)

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 11f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Unacceptable(11f)))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 10f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Improving(10f)))
    }

    @Test
    fun `currentAccuracy is never Poor when unacceptableAccuracyThreshold is less than accuracyThreshold + 5`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 5f, unacceptableAccuracyThreshold = 9f)

        val currentAccuracy = liveDataTester.activate(viewModel.currentAccuracy)

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 10f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Unacceptable(10f)))

        whenever(locationTracker.getCurrentLocation()).thenReturn(Location(0.0, 0.0, 0.0, 9f))
        scheduler.runForeground()
        assertThat(currentAccuracy.value, equalTo(GeoPointAccuracy.Improving(9f)))
    }

    @Test
    fun `onCleared() stops LocationTracker`() {
        val viewModel = createViewModel()
        viewModel.start()
        viewModel.onCleared()
    }

    @Test
    fun `onCleared() cancels repeat`() {
        val viewModel = createViewModel()
        viewModel.start()
        viewModel.onCleared()
        assertThat(scheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun `forceLocation() sets acceptedLocation to location tracker location regardless of threshold`() {
        val viewModel = createViewModel()
        viewModel.start(accuracyThreshold = 1.0f,)

        val location = liveDataTester.activate(viewModel.acceptedLocation)
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
    fun `forceLocation() locks acceptedLocation to current one`() {
        val viewModel = createViewModel()
        viewModel.start()

        val location = liveDataTester.activate(viewModel.acceptedLocation)
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
        val viewModel = createViewModel(clock = { timeElapsed })
        viewModel.start()

        val timeElapsedLiveData = liveDataTester.activate(viewModel.timeElapsed)

        timeElapsed = 5L
        scheduler.runForeground()
        assertThat(timeElapsedLiveData.value, equalTo(5L))

        timeElapsed = 60L
        scheduler.runForeground()
        assertThat(timeElapsedLiveData.value, equalTo(60L))
    }

    @Test
    fun `satellites updates with satellitesUsedInLastFix`() {
        val satellitesUsedInLastFix = MutableNonNullLiveData(0)
        whenever(satelliteInfoClient.satellitesUsedInLastFix).thenReturn(satellitesUsedInLastFix)

        val viewModel = createViewModel()
        assertThat(viewModel.satellites.value, equalTo(0))

        satellitesUsedInLastFix.value = 6
        assertThat(viewModel.satellites.value, equalTo(6))
    }

    private fun createViewModel(clock: () -> Long = { 0 }) =
        LocationTrackerGeoPointViewModel(locationTracker, satelliteInfoClient, clock, scheduler)
}
