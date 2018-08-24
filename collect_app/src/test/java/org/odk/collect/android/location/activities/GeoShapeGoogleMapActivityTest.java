package org.odk.collect.android.location.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.activities.GeoShapeGoogleMapActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoShapeGoogleMapActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoShapeGoogleMapActivity> activityController;

    private GeoShapeGoogleMapActivity activity;

    @Mock
    LocationClient locationClient;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        activityController = Robolectric.buildActivity(GeoShapeGoogleMapActivity.class);
        activity = activityController.get();

        LocationClients.setTestClient(locationClient);
    }

    @Test
    public void activityShouldShowZoomDialogOnFirstLocation() {
        activityController.create();

        activityController.start();
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);

        assertFalse(activity.getGpsButton().isEnabled());
        activity.onClientStart();
        assertTrue(activity.getGpsButton().isEnabled());

        verify(locationClient).requestLocationUpdates(activity);

        assertNull(activity.getZoomDialog());
        activity.onLocationChanged(newMockLocation());
        assertNotNull(activity.getZoomDialog());

        assertTrue(activity.getZoomDialog().isShowing());
        activity.getZoomDialog().dismiss();

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