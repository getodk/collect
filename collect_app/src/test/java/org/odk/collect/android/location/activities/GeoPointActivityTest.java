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
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.widgets.GeoPointWidget;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static android.app.Activity.RESULT_OK;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.LOCATION_RESULT;
import static org.robolectric.Shadows.shadowOf;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoPointActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoPointActivity> activityController;

    private GeoPointActivity activity;
    private ShadowActivity shadowActivity;

    @Mock
    LocationClient locationClient;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        activityController = Robolectric.buildActivity(GeoPointActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);

        LocationClients.setTestClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {

        activityController.create();

        // Activity.onStart() should call LocationClient.start().
        activityController.start();
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);
        when(locationClient.getLastLocation()).thenReturn(newMockLocation());

        // Make sure we're requesting updates and logging our previous location:
        activity.onClientStart();
        verify(locationClient).requestLocationUpdates(activity);
        verify(locationClient).getLastLocation();

        // Simulate the location updating:
        Location firstLocation = newMockLocation();
        when(firstLocation.getAccuracy()).thenReturn(0.0f);

        activity.onLocationChanged(firstLocation);

        // First update should only change dialog message (to avoid network location bug):
        assertFalse(shadowActivity.isFinishing());
        assertEquals(
                activity.getDialogMessage(),
                activity.getAccuracyMessage(firstLocation)
        );

        // Second update with poor accuracy should change dialog message:
        float poorAccuracy = (float) GeoPointWidget.DEFAULT_LOCATION_ACCURACY + 1.0f;

        Location secondLocation = newMockLocation();
        when(secondLocation.getAccuracy()).thenReturn(poorAccuracy);

        activity.onLocationChanged(secondLocation);

        assertFalse(shadowActivity.isFinishing());
        assertEquals(
                activity.getDialogMessage(),
                activity.getProviderAccuracyMessage(secondLocation)
        );

        // Third location with good accuracy should change dialog and finish activity.
        float goodAccuracy = (float) GeoPointWidget.DEFAULT_LOCATION_ACCURACY - 1.0f;

        Location thirdLocation = newMockLocation();
        when(thirdLocation.getAccuracy()).thenReturn(goodAccuracy);

        activity.onLocationChanged(thirdLocation);

        assertTrue(shadowActivity.isFinishing());
        assertEquals(
                activity.getDialogMessage(),
                activity.getProviderAccuracyMessage(thirdLocation)
        );

        assertEquals(shadowActivity.getResultCode(), RESULT_OK);

        Intent resultIntent = shadowActivity.getResultIntent();
        String resultString = resultIntent.getStringExtra(LOCATION_RESULT);

        assertEquals(resultString, activity.getResultStringForLocation(thirdLocation));
    }

    @Test
    public void activityShouldOpenSettingsIfLocationUnavailable() {
        activityController.create();
        activityController.start();

        when(locationClient.isLocationAvailable()).thenReturn(false);

        activity.onClientStart();
        assertTrue(shadowActivity.isFinishing());

        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Test
    public void activityShouldOpenSettingsIfLocationClientCantConnect() {
        activityController.create();
        activityController.start();

        activity.onClientStartFailure();
        assertTrue(shadowActivity.isFinishing());

        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Test
    public void activityShouldShutOffLocationClientWhenItStops() {
        activityController.create();
        activityController.start();

        verify(locationClient).start();

        activityController.stop();

        verify(locationClient).stop();
    }

    public static Location newMockLocation() {
        return mock(Location.class);
    }
}