package org.odk.collect.location.tracker

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.location.Location

abstract class LocationTrackerTest {

    abstract val locationTracker: LocationTracker

    abstract fun runBackground()
    abstract fun setDeviceLocation(location: Location)

    @Test
    fun `location is null when updates occur before starting`() {
        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))
        runBackground()

        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun `updates location when started`() {
        locationTracker.start()
        runBackground()

        val location = Location(1.0, 2.0, 3.0, 4.0f)
        setDeviceLocation(location)
        assertThat(locationTracker.getCurrentLocation(), equalTo(location))
    }

    @Test
    fun `updates location when started in foreground`() {
        locationTracker.start(background = false)

        val location = Location(1.0, 2.0, 3.0, 4.0f)
        setDeviceLocation(location)
        assertThat(locationTracker.getCurrentLocation(), equalTo(location))
    }

    @Test
    fun `location is null when updates occur after stopping`() {
        locationTracker.start()
        locationTracker.stop()
        runBackground()

        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))
        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun `location is null when updates occur after stopping from foreground`() {
        locationTracker.start(background = false)
        locationTracker.stop()

        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))
        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun `#stop clears location`() {
        locationTracker.start()
        runBackground()

        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))

        locationTracker.stop()
        runBackground()
        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun `#stop clears location from foreground`() {
        locationTracker.start(background = false)

        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))

        locationTracker.stop()
        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun `location is updated after restarting`() {
        val location = locationTracker.getLocation()

        locationTracker.start()
        runBackground()

        setDeviceLocation(Location(1.0, 1.0))
        runBackground()

        locationTracker.stop()
        runBackground()

        locationTracker.start()
        runBackground()

        setDeviceLocation(Location(2.0, 2.0))
        runBackground()
        assertThat(location.value, equalTo(Location(2.0, 2.0)))
    }

    @Test
    fun `location is updated after restarting in foreground`() {
        val location = locationTracker.getLocation()

        locationTracker.start(background = false)
        setDeviceLocation(Location(1.0, 1.0))
        locationTracker.stop()

        locationTracker.start(background = false)
        setDeviceLocation(Location(2.0, 2.0))
        assertThat(location.value, equalTo(Location(2.0, 2.0)))
    }

    @Test(expected = IllegalStateException::class)
    fun `#start in foreground after a #start in background fails`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = true)
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = false)
    }

    @Test
    fun `#start in foreground after a #start and #stop in background works`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = true)
        locationTracker.stop()
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = false)
    }

    @Test(expected = IllegalStateException::class)
    fun `#start in background after a #start in foreground fails`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = false)
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = true)
    }

    @Test
    fun `#start in background after a #start and #stop in foreground works`() {
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = false)
        locationTracker.stop()
        locationTracker.start(retainMockAccuracy = false, updateInterval = 1000L, background = true)
    }
}
