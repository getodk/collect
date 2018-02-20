package org.odk.collect.android.location.client;


import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleLocationClientTest {

    @Mock FusedLocationProviderApi fusedLocationProviderApi;
    @Mock GoogleApiClient googleApiClient;
    @Mock LocationManager locationManager;

    private GoogleLocationClient googleLocationClient;

    @Before
    public void setUp() {
        googleLocationClient = new GoogleLocationClient(googleApiClient, fusedLocationProviderApi, locationManager);
    }

    @Test
    public void startShouldCallLocationClientOnConnected() {
        doAnswer(new OnConnectedAnswer()).when(googleApiClient).connect();
        doAnswer(new OnDisconnectedAnswer()).when(googleApiClient).disconnect();

        TestClientListener testClientListener = new TestClientListener();
        googleLocationClient.setListener(testClientListener);

        googleLocationClient.start();
        assertTrue(testClientListener.wasStartCalled());
        assertFalse(testClientListener.wasStartFailureCalled());
        assertFalse(testClientListener.wasStopCalled());

        googleLocationClient.stop();
        assertTrue(testClientListener.wasStartCalled());
        assertFalse(testClientListener.wasStartFailureCalled());
        assertTrue(testClientListener.wasStopCalled());

        testClientListener.reset();

        doAnswer(new OnConnectionFailedAnswer()).when(googleApiClient).connect();

        googleLocationClient.start();
        assertFalse(testClientListener.wasStartCalled());
        assertTrue(testClientListener.wasStartFailureCalled());
        assertFalse(testClientListener.wasStopCalled());

        // Necessary for Stop to be called.
        when(googleApiClient.isConnected()).thenReturn(true);

        googleLocationClient.stop();
        assertFalse(testClientListener.wasStartCalled());
        assertTrue(testClientListener.wasStartFailureCalled());
        assertTrue(testClientListener.wasStopCalled());
    }

    @Test
    public void stopShouldDisconnectFromGoogleApiIfConnected() {

        TestClientListener testClientListener = new TestClientListener();
        googleLocationClient.setListener(testClientListener);

        // Call through to ApiClient.disconnect(), but don't do anything:
        when(googleApiClient.isConnected()).thenReturn(true);
        googleLocationClient.stop();

        // ApiClient.disconnect() won't call through to stop in this instance:
        assertFalse(testClientListener.wasStopCalled());

        doAnswer(new OnDisconnectedAnswer()).when(googleApiClient).disconnect();

        googleLocationClient.stop();
        assertTrue(testClientListener.wasStopCalled());

        testClientListener.reset();

        // Don't call through to ApiClient.disconnect():
        when(googleApiClient.isConnected()).thenReturn(false);
        googleLocationClient.stop();

        assertTrue(testClientListener.wasStopCalled());
    }

    @Test
    public void requestingLocationUpdatesShouldUpdateCorrectListener() {
        googleLocationClient.start();

        TestLocationListener firstListener = new TestLocationListener();
        googleLocationClient.requestLocationUpdates(firstListener);

        Location firstLocation = newMockLocation();
        googleLocationClient.onLocationChanged(firstLocation);

        assertSame(firstLocation, firstListener.getLastLocation());

        Location secondLocation = newMockLocation();
        googleLocationClient.onLocationChanged(secondLocation);

        assertSame(secondLocation, firstListener.getLastLocation());

        // Now stop updates:
        googleLocationClient.stopLocationUpdates();

        Location thirdLocation = newMockLocation();
        googleLocationClient.onLocationChanged(thirdLocation);

        // Should still be second:
        assertSame(secondLocation, firstListener.getLastLocation());

        // Call requestLocationUpdates again with new Listener:
        TestLocationListener secondListener = new TestLocationListener();
        googleLocationClient.requestLocationUpdates(secondListener);

        Location fourthLocation = newMockLocation();
        googleLocationClient.onLocationChanged(fourthLocation);

        // First listener should still have second location:
        assertSame(secondLocation, firstListener.getLastLocation());
        assertSame(fourthLocation, secondListener.getLastLocation());

        // Call stop() and make sure it called stopLocationUpdates():
        googleLocationClient.stop();

        Location fifthLocation = newMockLocation();
        googleLocationClient.onLocationChanged(fifthLocation);

        // Listener should still have fourth location:
        assertSame(fourthLocation, secondListener.getLastLocation());
    }

    @Test
    public void getLastLocationShouldCallBlockingConnectIfNotConnected() {
        googleLocationClient.getLastLocation();
        verify(googleApiClient).blockingConnect();

        when(googleApiClient.isConnected()).thenReturn(true);
        googleLocationClient.start();

        googleLocationClient.getLastLocation();
        verify(googleApiClient).blockingConnect(); // 'verify' checks if called *once*.
    }

    @Test
    public void canSetUpdateIntervalsShouldReturnTrue() {
        assertTrue(googleLocationClient.canSetUpdateIntervals());
    }

    private static Location newMockLocation() {
        return mock(Location.class);
    }

    private class OnConnectedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            googleLocationClient.onConnected(null);
            return null;
        }
    }

    private class OnConnectionFailedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            googleLocationClient.onConnectionFailed(new ConnectionResult(0));
            return null;
        }
    }

    private class OnDisconnectedAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            googleLocationClient.onConnectionSuspended(0);
            return null;
        }
    }
}
