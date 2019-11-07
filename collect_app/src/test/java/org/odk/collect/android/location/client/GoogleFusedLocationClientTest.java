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
public class GoogleFusedLocationClientTest {

    @Mock FusedLocationProviderApi fusedLocationProviderApi;
    @Mock GoogleApiClient googleApiClient;
    @Mock LocationManager locationManager;

    private GoogleFusedLocationClient client;

    @Before
    public void setUp() {
        client = new GoogleFusedLocationClient(googleApiClient, fusedLocationProviderApi, locationManager);
    }

    @Test
    public void startShouldCallLocationClientOnConnected() {
        doAnswer(new OnConnectedAnswer()).when(googleApiClient).connect();
        doAnswer(new OnDisconnectedAnswer()).when(googleApiClient).disconnect();

        TestClientListener testClientListener = new TestClientListener();
        client.setListener(testClientListener);

        client.start();
        assertTrue(testClientListener.wasStartCalled());
        assertFalse(testClientListener.wasStartFailureCalled());
        assertFalse(testClientListener.wasStopCalled());

        client.stop();
        assertTrue(testClientListener.wasStartCalled());
        assertFalse(testClientListener.wasStartFailureCalled());
        assertTrue(testClientListener.wasStopCalled());

        testClientListener.reset();

        doAnswer(new OnConnectionFailedAnswer()).when(googleApiClient).connect();

        client.start();
        assertFalse(testClientListener.wasStartCalled());
        assertTrue(testClientListener.wasStartFailureCalled());
        assertFalse(testClientListener.wasStopCalled());

        // Necessary for Stop to be called.
        when(googleApiClient.isConnected()).thenReturn(true);

        client.stop();
        assertFalse(testClientListener.wasStartCalled());
        assertTrue(testClientListener.wasStartFailureCalled());
        assertTrue(testClientListener.wasStopCalled());
    }

    @Test
    public void stopShouldDisconnectFromGoogleApiIfConnected() {

        TestClientListener testClientListener = new TestClientListener();
        client.setListener(testClientListener);

        // Call through to ApiClient.disconnect(), but don't do anything:
        when(googleApiClient.isConnected()).thenReturn(true);
        client.stop();

        // ApiClient.disconnect() won't call through to stop in this instance:
        assertFalse(testClientListener.wasStopCalled());

        doAnswer(new OnDisconnectedAnswer()).when(googleApiClient).disconnect();

        client.stop();
        assertTrue(testClientListener.wasStopCalled());

        testClientListener.reset();

        // Don't call through to ApiClient.disconnect():
        when(googleApiClient.isConnected()).thenReturn(false);
        client.stop();

        assertTrue(testClientListener.wasStopCalled());
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
    public void canSetUpdateIntervalsShouldReturnTrue() {
        assertTrue(client.canSetUpdateIntervals());
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
