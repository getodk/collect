package org.odk.collect.android.location.activities;

import android.content.Intent;
import android.location.Location;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static android.app.Activity.RESULT_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.LOCATION_RESULT;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;
import static org.robolectric.Shadows.shadowOf;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoPointMapActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoPointMapActivity> activityController;

    private GeoPointMapActivity activity;
    private ShadowActivity shadowActivity;

    @Mock
    LocationClient locationClient;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        activityController = Robolectric.buildActivity(GeoPointMapActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);

        LocationClients.setTestClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {
        activityController.create();

        // Create should prepare map async; onClientStartWill start location monitoring.
        // Whichever happens second will pass forward to upMyLocationOverlayLayers.
        when(locationClient.isMonitoringLocation()).thenReturn(true);
        activity.setMapReady(true);
        activity.setCaptureLocation(true);

        when(locationClient.isLocationAvailable()).thenReturn(true);

        activity.onClientStart();
        verify(locationClient).requestLocationUpdates(activity);

        assertNull(activity.getErrorDialog());

        activity.onLocationChanged(newMockLocation());
        assertEquals(activity.getLocationStatus(), activity.getString(R.string.please_wait_long));
        assertNull(activity.getZoomDialog());

        Location location = newMockLocation();
        when(location.getProvider()).thenReturn("GPS");
        when(location.getAccuracy()).thenReturn(1.0f);
        when(location.getLatitude()).thenReturn(2.0);
        when(location.getLongitude()).thenReturn(3.0);
        when(location.getAltitude()).thenReturn(4.0);

        activity.onLocationChanged(location);

        assertEquals(activity.getLocationStatus(), activity.getAccuracyStringForLocation(location));
        assertNotNull(activity.getZoomDialog());

        activity.getZoomDialog().dismiss();

        activity.returnLocation();
        assertTrue(shadowActivity.isFinishing());

        assertEquals(shadowActivity.getResultCode(), RESULT_OK);

        Intent resultIntent = shadowActivity.getResultIntent();
        assertEquals(resultIntent.getStringExtra(LOCATION_RESULT), activity.getResultString(location));
    }

    @Test
    public void activityShouldOpenErrorDialogIfLocationIsUnavailable() {
        activityController.create();

        // Create should prepare map async; onClientStartWill start location monitoring.
        // Whichever happens second will pass forward to upMyLocationOverlayLayers.
        when(locationClient.isMonitoringLocation()).thenReturn(true);
        activity.setMapReady(true);

        when(locationClient.isLocationAvailable()).thenReturn(false);

        activity.onClientStart();
        verify(locationClient).requestLocationUpdates(activity);

        assertNotNull(activity.getErrorDialog());
    }
}
