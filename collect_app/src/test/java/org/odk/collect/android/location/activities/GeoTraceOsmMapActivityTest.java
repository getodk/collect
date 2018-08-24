package org.odk.collect.android.location.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.activities.GeoTraceOsmMapActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.spatial.MapHelper;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoTraceOsmMapActivityTest extends BaseGeoActivityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ActivityController<GeoTraceOsmMapActivity> activityController;

    private GeoTraceOsmMapActivity activity;

    @Mock
    LocationClient locationClient;

    @Mock
    MapView mapView;

    @Mock
    MapHelper mapHelper;

    @Mock
    IMapController mapController;

    @Mock
    MyLocationNewOverlay locationOverlay;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        activityController = Robolectric.buildActivity(GeoTraceOsmMapActivity.class);
        activity = activityController.get();

        LocationClients.setTestClient(locationClient);
        activity.setMapView(mapView);
        activity.setMyLocationOverlay(locationOverlay);
        activity.setHelper(mapHelper);

        when(mapView.getController()).thenReturn(mapController);
    }

    @Test
    public void testLocationClientLifecycle() {
        activityController.create();
        activityController.start();

        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);

        ArrayList<Overlay> overlays = new ArrayList<>();
        when(mapView.getOverlays()).thenReturn(overlays);

        // When the LocationClient starts, add the overlay and enable location:
        activity.onClientStart();

        verify(locationClient).requestLocationUpdates(activity);
        assertFalse(overlays.isEmpty());
        verify(locationOverlay).setEnabled(true);
        verify(locationOverlay).enableMyLocation();

        activity.setModeActive(true);

        GeoPoint geoPoint = mock(GeoPoint.class);
        when(locationOverlay.getMyLocation()).thenReturn(geoPoint);

        activity.onLocationChanged(newMockLocation());
        verify(mapController).setCenter(geoPoint);

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