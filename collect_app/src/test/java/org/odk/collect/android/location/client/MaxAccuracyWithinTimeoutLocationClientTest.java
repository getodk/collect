package org.odk.collect.android.location.client;

import android.location.Location;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.testshared.RobolectricHelpers;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.location.LocationTestUtils.createLocation;
import static org.robolectric.shadows.ShadowSystemClock.advanceBy;

@RunWith(AndroidJUnit4.class)
public class MaxAccuracyWithinTimeoutLocationClientTest {
    private FakeLocationClient fakeLocationClient;
    private TestLocationListener testLocationListener;

    private MaxAccuracyWithinTimeoutLocationClient maxAccuracyLocationClient;

    @Before
    public void setUp() {
        fakeLocationClient = new FakeLocationClient();
        testLocationListener = new TestLocationListener();

        maxAccuracyLocationClient = new MaxAccuracyWithinTimeoutLocationClient(fakeLocationClient, testLocationListener);
    }

    @Test
    public void requestingLocationUpdates_ShouldResultInUpdate() {
        maxAccuracyLocationClient.requestLocationUpdates(5);
        RobolectricHelpers.runLooper();

        Location location = createLocation("GPS", 1, 1, 1, 100);
        fakeLocationClient.receiveFix(location);

        assertEquals(location, testLocationListener.getLastLocation());
    }

    @Test
    public void denyingLocationPermissions_ShouldResultInNoUpdates() {
        fakeLocationClient.setFailOnRequest(true);

        maxAccuracyLocationClient.requestLocationUpdates(5);

        Location location = createLocation("GPS", 1, 1, 1, 100);
        fakeLocationClient.receiveFix(location);

        assertNull(testLocationListener.getLastLocation());
    }

    @Test
    public void onlyMoreAccurateFixes_ShouldResultInUpdates() {
        maxAccuracyLocationClient.requestLocationUpdates(5);
        RobolectricHelpers.runLooper();

        Location location = createLocation("GPS", 1, 1, 1, 100);
        fakeLocationClient.receiveFix(location);
        assertEquals(location, testLocationListener.getLastLocation());

        Location moreAccurateLocation = createLocation("GPS", 1, 1, 1, 2);
        fakeLocationClient.receiveFix(moreAccurateLocation);
        assertEquals(moreAccurateLocation, testLocationListener.getLastLocation());

        Location lessAccurateLocation = createLocation("GPS", 1, 1, 1, 10);
        fakeLocationClient.receiveFix(lessAccurateLocation);
        assertEquals(moreAccurateLocation, testLocationListener.getLastLocation());
    }

    @Test
    public void fixWithAccuracyAfterAFixWithout_ShouldResultInUpdate() {
        maxAccuracyLocationClient.requestLocationUpdates(5);
        RobolectricHelpers.runLooper();

        Location locationWithoutAccuracy = createLocation("GPS", 1, 1, 1);
        fakeLocationClient.receiveFix(locationWithoutAccuracy);
        assertEquals(locationWithoutAccuracy, testLocationListener.getLastLocation());

        Location locationWithAccuracy = createLocation("GPS", 5, 5, 5, 100);
        fakeLocationClient.receiveFix(locationWithAccuracy);
        assertEquals(locationWithAccuracy, testLocationListener.getLastLocation());
    }

    @Test
    public void fixWithoutAccuracyAfterAFixWith_ShouldNotResultInUpdate() {
        maxAccuracyLocationClient.requestLocationUpdates(5);
        RobolectricHelpers.runLooper();

        Location locationWithAccuracy = createLocation("GPS", 5, 5, 5, 100);
        fakeLocationClient.receiveFix(locationWithAccuracy);
        assertEquals(locationWithAccuracy, testLocationListener.getLastLocation());

        Location locationWithoutAccuracy = createLocation("GPS", 1, 1, 1);
        fakeLocationClient.receiveFix(locationWithoutAccuracy);
        assertEquals(locationWithAccuracy, testLocationListener.getLastLocation());
    }

    @Test
    public void requestingLocationUpdatesAgain_ShouldResetHighestAccuracy() {
        maxAccuracyLocationClient.requestLocationUpdates(5);
        RobolectricHelpers.runLooper();

        Location location = createLocation("GPS", 1, 1, 1, 2);
        fakeLocationClient.receiveFix(location);
        assertEquals(location, testLocationListener.getLastLocation());

        maxAccuracyLocationClient.requestLocationUpdates(5);

        Location lessAccurateLocation = createLocation("GPS", 1, 1, 1, 10);
        fakeLocationClient.receiveFix(lessAccurateLocation);
        assertEquals(lessAccurateLocation, testLocationListener.getLastLocation());
    }

    @Test
    public void timeoutSecondsPassing_ShouldStopUpdates() {
        int timeoutSeconds = 5;
        maxAccuracyLocationClient.requestLocationUpdates(timeoutSeconds);
        RobolectricHelpers.runLooper();
        advanceBy(timeoutSeconds, TimeUnit.SECONDS);
        RobolectricHelpers.runLooper();

        // verify that stop() was called so resources are released
        assertFalse(fakeLocationClient.isRunning());

        // verify that if the location client is stopped, location updates aren't sent
        Location location = createLocation("GPS", 1, 1, 1, 100);
        fakeLocationClient.receiveFix(location);
        assertNull(testLocationListener.getLastLocation());
    }

    @Test
    public void requestingLocationUpdatesAgain_ShouldResetTimeout() {
        int timeoutSeconds = 5;
        maxAccuracyLocationClient.requestLocationUpdates(timeoutSeconds);
        RobolectricHelpers.runLooper();

        // advance time but not enough for updates to stop
        advanceBy(timeoutSeconds - 1, TimeUnit.SECONDS);
        assertTrue(fakeLocationClient.isRunning());

        // initiate a new request
        maxAccuracyLocationClient.requestLocationUpdates(timeoutSeconds);

        // verify that the new request reset the timer
        advanceBy(timeoutSeconds - 1, TimeUnit.SECONDS);
        assertTrue(fakeLocationClient.isRunning());
    }
}
