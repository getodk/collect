package org.odk.collect.location

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.LocationRequest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.odk.collect.location.LocationClient.LocationClientListener
import org.odk.collect.testshared.LocationTestUtils.createLocation

@RunWith(AndroidJUnit4::class)
class GoogleFusedLocationClientTest {
    private val fusedLocationProviderClientWrapper = mock<FusedLocationProviderClientWrapper>()
    private val locationClientListener = mock<LocationClientListener>()
    private val locationListener = TestLocationListener()
    private lateinit var client: GoogleFusedLocationClient

    @Before
    fun setUp() {
        client = GoogleFusedLocationClient(fusedLocationProviderClientWrapper, mock())
    }

    @Test
    fun `#start sets listener and calls #onClientStart`() {
        client.start(locationClientListener)

        assertThat(client.getListener(), notNullValue())
        verify(locationClientListener).onClientStart()
    }

    @Test
    fun `#start starts FusedLocationProviderClientWrapper`() {
        client.start(locationClientListener)

        verify(fusedLocationProviderClientWrapper).start(isA<GoogleFusedLocationClient>())
    }

    @Test
    fun `#stop removes listener and calls #onClientStop`() {
        client.start(locationClientListener)
        client.stop()

        assertThat(client.getListener(), nullValue())
        verify(locationClientListener).onClientStop()
    }

    @Test
    fun `#stop stops location updates`() {
        val listener = TestLocationListener()
        client.requestLocationUpdates(listener)
        client.stop()

        verify(fusedLocationProviderClientWrapper).removeLocationUpdates()
    }

    @Test
    fun `#requestLocationUpdates starts location updates`() {
        val listener = TestLocationListener()
        client.requestLocationUpdates(listener)

        verify(fusedLocationProviderClientWrapper).requestLocationUpdates(any())
    }

    @Test
    fun `#requestLocationUpdates builds location request based on passed values`() {
        client.setPriority(LocationClient.Priority.PRIORITY_NO_POWER)
        client.setUpdateIntervals(10L, 5L)
        val listener = TestLocationListener()
        client.requestLocationUpdates(listener)

        verify(fusedLocationProviderClientWrapper).requestLocationUpdates(
            argThat { locationRequest: LocationRequest ->
                assertThat(locationRequest.priority, equalTo(LocationClient.Priority.PRIORITY_NO_POWER.value))
                assertThat(locationRequest.interval, equalTo(10L))
                assertThat(locationRequest.fastestInterval, equalTo(5L))
                true
            }
        )
    }

    @Test
    fun `#isMonitoringLocation returns false if listener is mot set`() {
        client.stopLocationUpdates()

        assertThat(client.isMonitoringLocation, equalTo(false))
    }

    @Test
    fun `#isMonitoringLocation returns true if listener is set`() {
        val listener = TestLocationListener()
        client.requestLocationUpdates(listener)

        assertThat(client.isMonitoringLocation, equalTo(true))
    }

    @Test
    fun `#stopLocationUpdates stops location updates`() {
        val listener = TestLocationListener()
        client.requestLocationUpdates(listener)
        client.stopLocationUpdates()

        verify(fusedLocationProviderClientWrapper).removeLocationUpdates()
    }

    @Test
    fun `when received location accuracy is negative set to zero`() {
        client.requestLocationUpdates(locationListener)
        val location = createLocation("GPS", 7.0, 2.0, 3.0, -1f)
        client.onLocationChanged(location)

        assertThat(locationListener.lastLocation!!.accuracy, equalTo(0.0f))
    }

    @Test
    fun `when received location accuracy is mocked set to zero`() {
        client.requestLocationUpdates(locationListener)
        val location = createLocation("GPS", 7.0, 2.0, 3.0, 5.0f, true)
        client.onLocationChanged(location)

        assertThat(locationListener.lastLocation!!.accuracy, equalTo(0.0f))
    }

    @Test
    fun `when received location accuracy is mocked and #retainMockAccuracy is true do not change accuracy`() {
        client.setRetainMockAccuracy(true)
        client.requestLocationUpdates(locationListener)
        val location = createLocation("GPS", 7.0, 2.0, 3.0, 5.0f, true)
        client.onLocationChanged(location)

        assertThat(locationListener.lastLocation!!.accuracy, equalTo(5.0f))
    }
}
