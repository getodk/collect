package org.odk.collect.android.location;


import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.widgets.GeoPointWidget;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowProgressDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * Unit test for checking {@link Button}'s behaviour  in {@link MainMenuActivity}
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoPointActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoPointActivity> activityController;

    private GeoPointActivity activity;
    private ShadowActivity shadowActivity;

    @Mock LocationClient locationClient;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        activityController = Robolectric.buildActivity(GeoPointActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);

        activity.setLocationClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {

        // Make sure our Mock didn't get overriden:
        activityController.create();
        assertSame(activity.getLocationClient(), locationClient);

        ShadowProgressDialog shadowProgressDialog =
                shadowOf(activity.getLocationDialog());

        // Activity.onStart() should call LocationClient.start().
        activityController.start();
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);

        // Make sure we're requesting updates and logging our previous location:
        activity.onClientStart();
        verify(locationClient).requestLocationUpdates(activity);
        verify(locationClient.getLastLocation());

        // Simulate the location updating:
        Location firstLocation = newMockLocation();
        when(firstLocation.getAccuracy()).thenReturn(0.0f);

        activity.onLocationChanged(firstLocation);

        // First update should only change dialog message (to avoid network location bug):
        assertFalse(shadowActivity.isFinishing());
        assertEquals(
                shadowProgressDialog.getMessage(),
                activity.getAccuracyMessage(firstLocation)
        );

        // Second update with poor accuracy should change dialog message:
        float poorAccuracy = (float) GeoPointWidget.DEFAULT_LOCATION_ACCURACY + 1.0f;

        Location secondLocation = newMockLocation();
        when(secondLocation.getAccuracy()).thenReturn(poorAccuracy);

        activity.onLocationChanged(secondLocation);

        assertFalse(shadowActivity.isFinishing());
        assertEquals(
                shadowProgressDialog.getMessage(),
                activity.getProviderAccuracyMessage(secondLocation)
        );

        // Third location with good accuracy should change dialog and finish activity.
        float goodAccuracy = (float) GeoPointWidget.DEFAULT_LOCATION_ACCURACY - 1.0f;

        Location thirdLocation = newMockLocation();
        when(thirdLocation.getAccuracy()).thenReturn(goodAccuracy);

        activity.onLocationChanged(secondLocation);

        assertTrue(shadowActivity.isFinishing());
        assertEquals(
                shadowProgressDialog.getMessage(),
                activity.getProviderAccuracyMessage(thirdLocation)
        );

        Intent resultIntent = shadowActivity.getResultIntent();
        assertEquals(shadowActivity.getResultCode(), RESULT_O);
    }

    private static Location newMockLocation() {
        return mock(Location.class);
    }
}