package org.odk.collect.location;

import android.location.Location;
import android.location.LocationManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_HIGH_ACCURACY;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_LOW_POWER;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_NO_POWER;
import static org.odk.collect.testshared.LocationTestUtils.createLocation;

@RunWith(AndroidJUnit4.class)
public class AndroidLocationClientTest {

    private LocationManager locationManager;
    private AndroidLocationClient androidLocationClient;

    @Before
    public void setUp() {
        locationManager = mock(LocationManager.class);
        androidLocationClient = new AndroidLocationClient(locationManager);
    }

    @Test
    public void startingWithProvidersEnabledShouldCallStartAndStop() {

        List<String> providers = asList(GPS_PROVIDER, NETWORK_PROVIDER);
        when(locationManager.getProviders(true)).thenReturn(providers);

        TestClientListener testListener = new TestClientListener();
        androidLocationClient.setListener(testListener);

        androidLocationClient.start();
        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        androidLocationClient.stop();
        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertTrue(testListener.wasStopCalled());
    }

    @Test
    public void startingWithoutProvidersEnabledShouldCallStartFailureAndStop() {
        List<String> providers = asList();
        when(locationManager.getProviders(true)).thenReturn(providers);

        TestClientListener testListener = new TestClientListener();
        androidLocationClient.setListener(testListener);

        // Without any providers enabled, start shouldn't be called, but startFailure should be:
        androidLocationClient.start();
        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        // Stop should still be called if the user calls stop:
        androidLocationClient.stop();
        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertTrue(testListener.wasStopCalled());
    }

    @Test
    public void requestingLocationUpdatesShouldUpdateCorrectListener() {
        List<String> providers = asList(GPS_PROVIDER, NETWORK_PROVIDER);
        when(locationManager.getProviders(true)).thenReturn(providers);

        androidLocationClient.start();

        TestLocationListener firstListener = new TestLocationListener();
        androidLocationClient.requestLocationUpdates(firstListener);

        Location firstLocation = newMockLocation();
        androidLocationClient.onLocationChanged(firstLocation);

        assertSame(firstLocation, firstListener.getLastLocation());

        Location secondLocation = newMockLocation();
        androidLocationClient.onLocationChanged(secondLocation);

        assertSame(secondLocation, firstListener.getLastLocation());

        // Now stop updates:
        androidLocationClient.stopLocationUpdates();

        Location thirdLocation = newMockLocation();
        androidLocationClient.onLocationChanged(thirdLocation);

        // Should still be second:
        assertSame(secondLocation, firstListener.getLastLocation());

        // Call requestLocationUpdates again with new Listener:
        TestLocationListener secondListener = new TestLocationListener();
        androidLocationClient.requestLocationUpdates(secondListener);

        Location fourthLocation = newMockLocation();
        androidLocationClient.onLocationChanged(fourthLocation);

        // First listener should still have second location:
        assertSame(secondLocation, firstListener.getLastLocation());
        assertSame(fourthLocation, secondListener.getLastLocation());

        // Call stop() and make sure it called stopUpdates():
        androidLocationClient.stop();

        Location fifthLocation = newMockLocation();
        androidLocationClient.onLocationChanged(fifthLocation);

        // Listener should still have fourth location:
        assertSame(fourthLocation, secondListener.getLastLocation());
    }

    @Test
    public void passiveProviderOnlyShouldFailOnHighAndBalancedPriorities() {
        List<String> highAccuracyProviders = asList(PASSIVE_PROVIDER);
        when(locationManager.getProviders(true)).thenReturn(highAccuracyProviders);

        TestClientListener testListener = new TestClientListener();
        androidLocationClient.setListener(testListener);

        // HIGH_ACCURACY and BALANCED_POWER_ACCURACY should fail with only
        // PASSIVE_PROVIDER enabled:
        androidLocationClient.setPriority(PRIORITY_HIGH_ACCURACY);
        androidLocationClient.start();

        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        androidLocationClient.start();

        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        // PRIORITY_LOW_POWER and PRIORITY_NO_POWER should succeed with only
        // PASSIVE_PROVIDER enabled:
        androidLocationClient.setPriority(PRIORITY_LOW_POWER);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_NO_POWER);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();
    }

    @Test
    public void networkProviderOnlyShouldFailOnNoPowerPriority() {
        List<String> highAccuracyProviders = asList(NETWORK_PROVIDER);
        when(locationManager.getProviders(true)).thenReturn(highAccuracyProviders);

        TestClientListener testListener = new TestClientListener();
        androidLocationClient.setListener(testListener);

        // PRIORITY_NO_POWER should fail with only
        // NETWORK_PROVIDER enabled:
        androidLocationClient.setPriority(PRIORITY_NO_POWER);
        androidLocationClient.start();

        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        // PRIORITY_LOW_POWER, PRIORITY_BALANCED_POWER_ACCURACY, and
        // PRIORITY_HIGH_ACCURACY should succeed with only
        // NETWORK_PROVIDER enabled:
        androidLocationClient.setPriority(PRIORITY_LOW_POWER);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_HIGH_ACCURACY);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();
    }

    @Test
    public void gpsProviderOnlyShouldFailOnLowAndNoPowerPriorities() {
        List<String> highAccuracyProviders = asList(GPS_PROVIDER);
        when(locationManager.getProviders(true)).thenReturn(highAccuracyProviders);

        TestClientListener testListener = new TestClientListener();
        androidLocationClient.setListener(testListener);

        // PRIORITY_NO_POWER and PRIORITY_LOW_POWER should fail with only
        // GPS_PROVIDER enabled:
        androidLocationClient.setPriority(PRIORITY_NO_POWER);
        androidLocationClient.start();

        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_LOW_POWER);
        androidLocationClient.start();

        assertFalse(testListener.wasStartCalled());
        assertTrue(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        // PRIORITY_BALANCED_POWER_ACCURACY, and PRIORITY_HIGH_ACCURACY
        // should succeed with only GPS_PROVIDER enabled:

        androidLocationClient.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();

        androidLocationClient.setPriority(PRIORITY_HIGH_ACCURACY);
        androidLocationClient.start();

        assertTrue(testListener.wasStartCalled());
        assertFalse(testListener.wasStartFailureCalled());
        assertFalse(testListener.wasStopCalled());

        testListener.reset();
    }

    @Test
    public void getLastLocationShouldReturnCorrectFromCorrectProviderForPriority() {
        // Set-up mock Locations: -------------------------------------------------------------- //

        Location gpsLocation = newMockLocation();
        when(locationManager.getLastKnownLocation(GPS_PROVIDER))
                .thenReturn(gpsLocation);

        Location networkLocation = newMockLocation();
        when(locationManager.getLastKnownLocation(NETWORK_PROVIDER))
                .thenReturn(networkLocation);

        Location passiveLocation = newMockLocation();
        when(locationManager.getLastKnownLocation(PASSIVE_PROVIDER))
                .thenReturn(passiveLocation);

        // High Accuracy: -------------------------------------------------------------- //

        androidLocationClient.setPriority(PRIORITY_HIGH_ACCURACY);

        // W/ GPS
        when(locationManager.getProviders(true))
                .thenReturn(asList(GPS_PROVIDER, NETWORK_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), gpsLocation);

        // W/out GPS:
        when(locationManager.getProviders(true))
                .thenReturn(asList(NETWORK_PROVIDER));

        assertSame(androidLocationClient.getLastLocation(), networkLocation);

        // Balanced Accuracy: -------------------------------------------------------------- //

        androidLocationClient.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

        // W/ both (should be Network)
        when(locationManager.getProviders(true))
                .thenReturn(asList(GPS_PROVIDER, NETWORK_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), networkLocation);

        // W/out Network
        when(locationManager.getProviders(true))
                .thenReturn(asList(GPS_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), gpsLocation);

        // Low Power Accuracy: -------------------------------------------------------------- //

        androidLocationClient.setPriority(PRIORITY_LOW_POWER);

        // W/ Network

        when(locationManager.getProviders(true))
                .thenReturn(asList(NETWORK_PROVIDER, PASSIVE_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), networkLocation);

        // W/out Network:
        when(locationManager.getProviders(true))
                .thenReturn(asList(PASSIVE_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), passiveLocation);

        // No Power: -------------------------------------------------------------- //
        androidLocationClient.setPriority(PRIORITY_NO_POWER);

        // W/ Passive:
        when(locationManager.getProviders(true))
                .thenReturn(asList(PASSIVE_PROVIDER));
        assertSame(androidLocationClient.getLastLocation(), passiveLocation);

        // W/out any Providers:
        when(locationManager.getProviders(true))
                .thenReturn(asList());

        assertNull(androidLocationClient.getLastLocation());
    }

    @Test
    public void canSetUpdateIntervalsShouldReturnFalse() {
        assertFalse(androidLocationClient.canSetUpdateIntervals());
    }

    @Test
    public void whenNewlyReceivedLocationAccuracyIsNegative_shouldBeSetToZero() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));

        androidLocationClient.start();

        TestLocationListener listener = new TestLocationListener();
        androidLocationClient.requestLocationUpdates(listener);

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, -1.0f);
        androidLocationClient.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenNewlyReceivedLocationIsMocked_shouldAccuracyBeSetToZero() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));

        androidLocationClient.start();

        TestLocationListener listener = new TestLocationListener();
        androidLocationClient.requestLocationUpdates(listener);

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        androidLocationClient.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenNewlyReceivedLocationIsMocked_andRetainMockAccuracyIsTrue_doesNotChangeAccuracy() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));

        androidLocationClient.setRetainMockAccuracy(true);
        androidLocationClient.start();

        TestLocationListener listener = new TestLocationListener();
        androidLocationClient.requestLocationUpdates(listener);

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        androidLocationClient.onLocationChanged(location);

        assertThat(listener.getLastLocation().getAccuracy(), is(5.0f));
    }

    @Test
    public void whenLastKnownLocationAccuracyIsNegative_shouldBeSetToZero() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, -1.0f);
        when(locationManager.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location);

        assertThat(androidLocationClient.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLastKnownLocationIsMocked_shouldAccuracyBeSetToZero() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        when(locationManager.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location);

        assertThat(androidLocationClient.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLastKnownLocationIsMocked_andRetainMockAccuracyIsTrue_doesNotChangeAccuracy() {
        when(locationManager.getProviders(true)).thenReturn(Collections.singletonList(GPS_PROVIDER));
        androidLocationClient.setRetainMockAccuracy(true);

        Location location = createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        when(locationManager.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location);

        assertThat(androidLocationClient.getLastLocation().getAccuracy(), is(5.0f));
    }

    private static Location newMockLocation() {
        return mock(Location.class);
    }
}
