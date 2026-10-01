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
import org.odk.collect.location.LocationClient.LocationClientListener
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ForegroundOrServiceLocationTrackerTest : LocationTrackerTest() {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val locationClient = FakeLocationClient()

    override val locationTracker: LocationTracker = ForegroundOrServiceLocationTracker(application)

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
    fun setup() {
        LocationClientProvider.setTestClient(locationClient)
    }

    @After
    fun teardown() {
        RobolectricHelpers.clearServices()
        LocationClientProvider.setTestClient(null)
    }

    @Test
    fun `#start when retain mock accuracy is true sets retain mock accuracy on client`() {
        locationTracker.start(retainMockAccuracy = true)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(true))
    }

    @Test
    fun `#start when retain mock accuracy is false sets retain mock accuracy on client`() {
        locationTracker.start(retainMockAccuracy = false)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(false))
    }

    @Test
    fun `#start when update interval is null does not set interval on client`() {
        locationTracker.start(updateInterval = null)
        runBackground()

        assertThat(locationClient.getUpdateInterval(), equalTo(null))
    }

    @Test
    fun `#start when update interval is non-null sets intervals on client`() {
        locationTracker.start(updateInterval = 1000)
        runBackground()

        assertThat(locationClient.getUpdateInterval(), equalTo(1000L))
    }

    @Test
    fun `#start after another #start updates client`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L)
        runBackground()

        LocationClientProvider.setTestClient(FakeLocationClient()) // Make sure we use same instance
        locationTracker.start(retainMockAccuracy = true, updateInterval = 2000L)
        runBackground()

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(true))
        assertThat(locationClient.getUpdateInterval(), equalTo(2000L))
    }

    @Test
    fun `#start after another #start when not in background updates client`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = false)

        LocationClientProvider.setTestClient(FakeLocationClient()) // Make sure we use same instance
        locationTracker.start(retainMockAccuracy = true, updateInterval = 2000L, background = false)

        assertThat(locationClient.getRetainMockAccuracy(), equalTo(true))
        assertThat(locationClient.getUpdateInterval(), equalTo(2000L))
    }
}

private class FakeLocationClient : LocationClient {

    private var started = false
    private var locationListener: LocationListener? = null
    private var locationClientListener: LocationClientListener? = null
    private var retainMockAccuracy: Boolean = false
    private var updateInterval: Long? = null

    override fun start(listener: LocationClientListener) {
        setListener(listener)
        this.started = true
        locationClientListener?.onClientStart()
    }

    override fun stop() {
        this.started = false
        locationClientListener?.onClientStop()
        setListener(null)
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

    override fun setUpdateInterval(updateInterval: Long) {
        this@FakeLocationClient.updateInterval = updateInterval
    }

    fun updateLocation(location: android.location.Location) {
        if (started) {
            locationListener?.onLocationChanged(location)
        }
    }

    fun getRetainMockAccuracy(): Boolean {
        return retainMockAccuracy
    }

    fun getUpdateInterval(): Long? {
        return updateInterval
    }
}
