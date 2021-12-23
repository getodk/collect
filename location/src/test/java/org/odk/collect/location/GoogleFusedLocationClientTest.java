package org.odk.collect.location;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;
import android.location.LocationManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.odk.collect.testshared.LocationTestUtils;

@RunWith(AndroidJUnit4.class)
public class GoogleFusedLocationClientTest {

    private GoogleApiClient googleApiClient;
    private GoogleFusedLocationClient client;
    private FusedLocationProviderApi fusedLocationProviderApi;

    @Before
    public void setUp() {
        fusedLocationProviderApi = mock(FusedLocationProviderApi.class);
        googleApiClient = mock(GoogleApiClient.class);
        LocationManager locationManager = mock(LocationManager.class);
        client = new GoogleFusedLocationClient(googleApiClient, fusedLocationProviderApi, locationManager);
    }

    @Test
    public void startShouldCallLocationClientOnConnected() {
        doAnswer(new OnConnectedAnswer()).when(googleApiClient).connect();

        LocationClient.LocationClientListener listener = mock(LocationClient.LocationClientListener.class);
        client.setListener(listener);

        client.start();
        verify(listener).onClientStart();
        verify(listener, never()).onClientStartFailure();
        verify(listener, never()).onClientStop();

        client.stop();

        reset(listener);
        doAnswer(new OnConnectionFailedAnswer()).when(googleApiClient).connect();

        client.start();
        verify(listener, never()).onClientStart();
        verify(listener).onClientStartFailure();
        verify(listener, never()).onClientStop();

        client.stop();
        verify(listener, never()).onClientStart();
        verify(listener).onClientStartFailure();
        verify(listener).onClientStop();
    }

    @Test
    public void stopShouldDisconnectFromGoogleApiIfConnected_andAlwaysCallOnClientStopIfListenerSet() {
        LocationClient.LocationClientListener listener = mock(LocationClient.LocationClientListener.class);
        client.setListener(listener);

        // Previously connected, disconnection succeeds
        when(googleApiClient.isConnected()).thenReturn(true);
        client.stop();
        verify(googleApiClient).disconnect();
        verify(listener).onClientStop();

        reset(listener);

        // Previously connected, disconnection calls onConnectionSuspended
        doAnswer(new OnDisconnectedAnswer()).when(googleApiClient).disconnect();
        client.stop();
        verify(listener).onClientStop();

        reset(listener);

        // Not previously connected
        when(googleApiClient.isConnected()).thenReturn(false);
        client.stop();
        verify(listener).onClientStop();
    }

    @Test
    public void whenGoogleApiClientNotConnected_shouldNotRemoveLocationUpdatesBeCalled() {
        when(googleApiClient.isConnected()).thenReturn(false);

        client.start();

        TestLocationListener listener = new TestLocationListener();
        client.requestLocationUpdates(listener);

        client.stop();
        verify(fusedLocationProviderApi, never()).removeLocationUpdates(googleApiClient, listener);
    }

    @Test
    public void whenGoogleApiClientNotConnected_shouldNotRequestLocationUpdatesBeCalled() {
        when(googleApiClient.isConnected()).thenReturn(false);

        client.start();
        client.requestLocationUpdates(new TestLocationListener());

        verify(fusedLocationProviderApi, never()).requestLocationUpdates(any(), any(), (LocationListener) any());
    }

    @Test
    public void requestingLocationUpdatesShouldUpdateCorrectListener() {
        client.start();

        TestLocationListener firstListener = new TestLocationListener();
        client.requestLocationUpdates(firstListener);

        Location firstLocation = newMockLocation();
        client.onLocationChanged(firstLocation);

        assertSame(firstLocation, firstListener.getLastLocation());

        Location secondLocation = newMockLocation();
        client.onLocationChanged(secondLocation);

        assertSame(secondLocation, firstListener.getLastLocation());

        // Now stop updates:
        client.stopLocationUpdates();

        Location thirdLocation = newMockLocation();
        client.onLocationChanged(thirdLocation);

        // Should still be second:
        assertSame(secondLocation, firstListener.getLastLocation());

        // Call requestLocationUpdates again with new listener:
        TestLocationListener secondListener = new TestLocationListener();
        client.requestLocationUpdates(secondListener);

        Location fourthLocation = newMockLocation();
        client.onLocationChanged(fourthLocation);

        // First listener should still have second location:
        assertSame(secondLocation, firstListener.getLastLocation());
        assertSame(fourthLocation, secondListener.getLastLocation());

        // Call stop() and make sure it called stopLocationUpdates():
        client.stop();

        Location fifthLocation = newMockLocation();
        client.onLocationChanged(fifthLocation);

        // Listener should still have fourth location:
        assertSame(fourthLocation, secondListener.getLastLocation());
    }

    @Test
    public void getLastLocationShouldCallBlockingConnectIfNotConnected() {
        client.getLastLocation();
        verify(googleApiClient).blockingConnect();

        when(googleApiClient.isConnected()).thenReturn(true);
        client.start();

        client.getLastLocation();
        verify(googleApiClient).blockingConnect(); // 'verify' checks if called *once*.
    }

    @Test
    public void whenNewlyReceivedLocationAccuracyIsNegative_shouldBeSetToZero() {
        TestLocationListener listener = new TestLocationListener();
        client.requestLocationUpdates(listener);

        Location location = LocationTestUtils.createLocation("GPS", 7, 2, 3, -1);
        client.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenNewlyReceivedLocationIsMocked_shouldAccuracyBeSetToZero() {
        TestLocationListener listener = new TestLocationListener();
        client.requestLocationUpdates(listener);

        Location location = LocationTestUtils.createLocation("GPS", 7, 2, 3, 5.0f, true);
        client.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenNewlyReceivedLocationIsMocked_andRetainMockAccuracyIsTrue_doesNotChangeAccuracy() {
        TestLocationListener listener = new TestLocationListener();
        client.setRetainMockAccuracy(true);
        client.requestLocationUpdates(listener);

        Location location = LocationTestUtils.createLocation("GPS", 7, 2, 3, 5.0f, true);
        client.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(5.0f));
    }

    @Test
    public void whenLastKnownLocationAccuracyIsNegative_shouldBeSetToZero() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7, 2, 3, -1.0f);
        when(fusedLocationProviderApi.getLastLocation(googleApiClient)).thenReturn(location);

        assertThat(client.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLastKnownLocationIsMocked_shouldAccuracyBeSetToZero() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        when(fusedLocationProviderApi.getLastLocation(googleApiClient)).thenReturn(location);

        assertThat(client.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLastKnownLocationIsMocked_andRetainMockAccuracyIsTrue_doesNotChangeAccuracy() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        client.setRetainMockAccuracy(true);
        when(fusedLocationProviderApi.getLastLocation(googleApiClient)).thenReturn(location);

        assertThat(client.getLastLocation().getAccuracy(), is(5.0f));
    }

    private static Location newMockLocation() {
        return mock(Location.class);
    }

    private class OnConnectedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            client.onConnected(null);
            return null;
        }
    }

    private class OnConnectionFailedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            client.onConnectionFailed(new ConnectionResult(0));
            return null;
        }
    }

    private class OnDisconnectedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            client.onConnectionSuspended(0);
            return null;
        }
    }
}
