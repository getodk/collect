package org.odk.collect.android.location.activities;

import android.content.Intent;
import android.location.Location;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.location.LocationClient;
import org.odk.collect.location.LocationClientProvider;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import static android.app.Activity.RESULT_OK;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.utilities.GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class GeoPointActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoPointActivity> activityController;

    private GeoPointActivity activity;

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
        LocationClientProvider.setTestClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {

        activityController.create();
        activityController.start();

        // Activity.onResume() should call LocationClient.start().
        activityController.resume();
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

        // First update should never result in a selected point to avoid network location bug:
        assertFalse(activity.isFinishing());
        assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(firstLocation)));

        // Second update with poor accuracy should change dialog message:
        float poorAccuracy = (float) DEFAULT_LOCATION_ACCURACY + 1.0f;

        Location secondLocation = newMockLocation();
        when(secondLocation.getAccuracy()).thenReturn(poorAccuracy);

        activity.onLocationChanged(secondLocation);

        assertFalse(activity.isFinishing());
        assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(secondLocation)));

        // Third location with good accuracy should change dialog and finish activity.
        float goodAccuracy = (float) DEFAULT_LOCATION_ACCURACY - 1.0f;

        Location thirdLocation = newMockLocation();
        when(thirdLocation.getAccuracy()).thenReturn(goodAccuracy);

        activity.onLocationChanged(thirdLocation);

        assertTrue(activity.isFinishing());
        assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(thirdLocation)));

        assertEquals(shadowOf(activity).getResultCode(), RESULT_OK);

        Intent resultIntent = shadowOf(activity).getResultIntent();
        String resultString = resultIntent.getStringExtra(FormEntryActivity.ANSWER_KEY);

        assertEquals(resultString, activity.getResultStringForLocation(thirdLocation));
    }

    @Test
    public void activityShouldOpenSettingsIfLocationUnavailable() {
        activityController.create();
        activityController.start();

        when(locationClient.isLocationAvailable()).thenReturn(false);

        activity.onClientStart();
        assertTrue(activity.isFinishing());

        Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
        assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Test
    public void activityShouldOpenSettingsIfLocationClientCantConnect() {
        activityController.create();
        activityController.start();

        activity.onClientStartFailure();
        assertTrue(activity.isFinishing());

        Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
        assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Test
    public void activityShouldShutOffLocationClientWhenItPauses() {
        activityController.create();
        activityController.start();
        activityController.resume();

        verify(locationClient).start();

        activityController.pause();
        verify(locationClient).stop();
    }

    public static Location newMockLocation() {
        return mock(Location.class);
    }
}
