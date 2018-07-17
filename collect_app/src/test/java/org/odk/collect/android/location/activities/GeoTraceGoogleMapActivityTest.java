package org.odk.collect.android.location.activities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.activities.GeoTraceGoogleMapActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoTraceGoogleMapActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoTraceGoogleMapActivity> activityController;

    private GeoTraceGoogleMapActivity activity;

    @Mock
    LocationClient locationClient;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        activityController = Robolectric.buildActivity(GeoTraceGoogleMapActivity.class);
        activity = activityController.get();

        LocationClients.setTestClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {
        activityController.create();
        activityController.start();

        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);

        Location location = newMockLocation();
        when(locationClient.getLastLocation()).thenReturn(location);

        assertNull(activity.getCurLocation());

        activity.onClientStart();

        verify(locationClient).requestLocationUpdates(activity);
        verify(locationClient).getLastLocation();

        assertSame(activity.getCurLocation(), location);

        Location newLocation = newMockLocation();
        when(newLocation.getLatitude()).thenReturn(1.0);
        when(newLocation.getLongitude()).thenReturn(2.0);

        activity.setModeActive(false);
        activity.getPlayButton().setEnabled(false);

        activity.onLocationChanged(newLocation);

        assertTrue(activity.getPlayButton().isEnabled());
        assertSame(newLocation, activity.getCurLocation());
        assertEquals(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()), activity.getCurlatLng());

        activityController.stop();
        verify(locationClient).stop();
    }


    @Test
    public void activityShouldShowErrorDialogOnClientError() {
        activityController.create();
        activityController.start();

        assertNull(activity.getErrorDialog());

        activity.onClientStartFailure();

        assertNotNull(activity.getErrorDialog());
        assertTrue(activity.getErrorDialog().isShowing());
    }

    @Test
    public void activityShouldShowErrorDialogIfLocationUnavailable() {
        activityController.create();
        activityController.start();

        when(locationClient.isLocationAvailable()).thenReturn(false);

        assertNull(activity.getErrorDialog());

        activity.onClientStart();

        assertNotNull(activity.getErrorDialog());
        assertTrue(activity.getErrorDialog().isShowing());
    }
}