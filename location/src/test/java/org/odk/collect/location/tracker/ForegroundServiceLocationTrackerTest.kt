package org.odk.collect.location.tracker

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.LocationListener
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ForegroundServiceLocationTrackerTest : LocationTrackerTest() {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val locationClient = FakeLocationClient()

    override val locationTracker: LocationTracker = ForegroundServiceLocationTracker(application)

    override fun runBackground() {
        RobolectricHelpers.runServices(true)
    }

    override fun setDeviceLocation(location: Location) {
        val androidLocation = android.location.Location("gps")
        androidLocation.latitude = location.latitude
        androidLocation.longitude = location.longitude
        androidLocation.altitude = location.altitude
        androidLocation.accuracy = location.accuracy

        locationClient.updateLocation(androidLocation)
    }

    @Before
    fun fakeLocationClient() {
        LocationClientProvider.setTestClient(locationClient)
    }

    @After
    fun teardown() {
        RobolectricHelpers.clearServices()
        LocationClientProvider.setTestClient(null)
    }

    @Test
    fun start_whenRetainMockAccuracyIsTrue_setsRetainMockAccuracyOnClient() {
        locationTracker.start(retainMockAccuracy = true)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(true))
    }

    @Test
    fun start_whenRetainMockAccuracyIsFalse_setsRetainMockAccuracyOnClient() {
        locationTracker.start(retainMockAccuracy = false)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(false))
    }

    @Test
    fun start_whenUpdateIntervalIsNull_doesNotSetIntervalOnClient() {
        locationTracker.start(updateInterval = null)
        runBackground()

        assertThat(locationClient.getUpdateIntervals(), equalTo(null))
    }

    @Test
    fun start_whenUpdateIntervalIsNonNull_setsIntervalsOnClient() {
        locationTracker.start(updateInterval = 1000)
        runBackground()

        assertThat(locationClient.getUpdateIntervals(), equalTo(Pair(1000L, 500L)))
    }

    @Test
    fun start_afterAnotherStart_updatesClient() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L)
        runBackground()

        locationTracker.start(retainMockAccuracy = true, updateInterval = 2000L)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(true))
        assertThat(locationClient.getUpdateIntervals(), equalTo(Pair(2000L, 1000L)))
    }
}

private class FakeLocationClient : LocationClient {

    private var started = false
    private var locationListener: LocationListener? = null
    private var locationClientListener: LocationClient.LocationClientListener? = null
    private var retainMockAccuracy: Boolean = false
    private var updateIntervals: Pair<Long, Long>? = null

    override fun start() {
        this.started = true
        locationClientListener?.onClientStart()
    }

    override fun stop() {
        this.started = false
        locationClientListener?.onClientStop()
    }

    override fun requestLocationUpdates(locationListener: LocationListener) {
        if (!started) {
            throw IllegalStateException("Can't request location updated before starting!")
        }

        this.locationListener = locationListener
    }

    override fun stopLocationUpdates() {
        TODO("Not yet implemented")
    }

    override fun setListener(locationClientListener: LocationClient.LocationClientListener?) {
        this.locationClientListener = locationClientListener
    }

    override fun setPriority(priority: LocationClient.Priority) {
        TODO("Not yet implemented")
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        this.retainMockAccuracy = retainMockAccuracy
    }

    override fun getLastLocation(): android.location.Location? {
        TODO("Not yet implemented")
    }

    override fun isLocationAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isMonitoringLocation(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setUpdateIntervals(updateInterval: Long, fastestUpdateInterval: Long) {
        updateIntervals = Pair(updateInterval, fastestUpdateInterval)
    }

    fun updateLocation(location: android.location.Location) {
        if (started) {
            locationListener?.onLocationChanged(location)
        }
    }

    fun getRetainMockAccuracy(): Boolean {
        return retainMockAccuracy
    }

    fun getUpdateIntervals(): Pair<Long, Long>? {
        return updateIntervals
    }
}
