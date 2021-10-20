package org.odk.collect.geo;

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
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.location.Location;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.externalapp.ExternalAppUtils;
import org.odk.collect.location.LocationClient;
import org.odk.collect.location.LocationClientProvider;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AndroidJUnit4.class)
public class GeoPointActivityTest {

    LocationClient locationClient = mock(LocationClient.class);

    @Before
    public void setUp() throws Exception {
        ShadowApplication shadowApplication = shadowOf(ApplicationProvider.<Application>getApplicationContext());
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION");

        LocationClientProvider.setTestClient(locationClient);
    }

    @Test
    public void testLocationClientLifecycle() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        intent.putExtra(GeoPointActivity.EXTRA_ACCURACY_THRESHOLD, 5.0);
        ActivityScenario<GeoPointActivity> scenario = ActivityScenario.launch(intent);

        // Activity.onResume() should call LocationClient.start().
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);
        when(locationClient.getLastLocation()).thenReturn(mock(Location.class));

        // Make sure we're requesting updates and logging our previous location:
        scenario.onActivity(activity -> {
            activity.onClientStart();
            verify(locationClient).requestLocationUpdates(activity);
            verify(locationClient).getLastLocation();
        });


        // Simulate the location updating:
        Location firstLocation = mock(Location.class);
        when(firstLocation.getAccuracy()).thenReturn(0.0f);

        scenario.onActivity(activity -> {
            activity.onLocationChanged(firstLocation);

            // First update should never result in a selected point to avoid network location bug:
            assertFalse(activity.isFinishing());
            assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(firstLocation)));
        });

        // Second update with poor accuracy should change dialog message:
        float poorAccuracy = (float) 6.0;

        Location secondLocation = mock(Location.class);
        when(secondLocation.getAccuracy()).thenReturn(poorAccuracy);

        scenario.onActivity(activity -> {
            activity.onLocationChanged(secondLocation);

            assertFalse(activity.isFinishing());
            assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(secondLocation)));
        });

        // Third location with good accuracy should change dialog and finish activity.
        float goodAccuracy = (float) 4.0;

        Location thirdLocation = mock(Location.class);
        when(thirdLocation.getAccuracy()).thenReturn(goodAccuracy);

        scenario.onActivity(activity -> {
            activity.onLocationChanged(thirdLocation);

            assertTrue(activity.isFinishing());
            assertThat(activity.getDialogMessage(), containsString(activity.getAccuracyMessage(thirdLocation)));
        });

        assertEquals(scenario.getResult().getResultCode(), RESULT_OK);

        Intent resultIntent = scenario.getResult().getResultData();
        String resultString = ExternalAppUtils.getReturnedSingleValue(resultIntent);

        scenario.onActivity(activity -> {
            assertEquals(resultString, activity.getResultStringForLocation(thirdLocation));
        });
    }

    @Test
    public void activityShouldOpenSettingsIfLocationUnavailable() {
        when(locationClient.isLocationAvailable()).thenReturn(false);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        ActivityScenario<GeoPointActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            activity.onClientStart();
            assertTrue(activity.isFinishing());

            Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
            assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
        });
    }

    @Test
    public void activityShouldOpenSettingsIfLocationClientCantConnect() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        ActivityScenario<GeoPointActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            activity.onClientStartFailure();
            assertTrue(activity.isFinishing());

            Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
            assertEquals(nextStartedActivity.getAction(), ACTION_LOCATION_SOURCE_SETTINGS);
        });
    }

    @Test
    public void activityShouldShutOffLocationClientWhenItPauses() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        ActivityScenario<GeoPointActivity> scenario = ActivityScenario.launch(intent);

        verify(locationClient).start();
        scenario.moveToState(Lifecycle.State.STARTED);
        verify(locationClient).stop();
    }

    @Test
    public void setsLocationClientRetainMockAccuracyToFalse() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        ActivityScenario.launch(intent);
        verify(locationClient).setRetainMockAccuracy(false);
    }

    @Test
    public void passingRetainMockAccuracyExtra_showSetItOnLocationClient() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointActivity.class);
        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true);
        ActivityScenario.launch(intent);
        verify(locationClient).setRetainMockAccuracy(true);

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, false);
        ActivityScenario.launch(intent);
        verify(locationClient).setRetainMockAccuracy(false);
    }
}
