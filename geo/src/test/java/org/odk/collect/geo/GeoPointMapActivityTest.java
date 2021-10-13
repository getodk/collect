package org.odk.collect.geo;

import static android.app.Activity.RESULT_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapFragmentFactory;
import org.odk.collect.geo.maps.MapPoint;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GeoPointMapActivityTest {

    private final FakeMapFragment mapFragment = new FakeMapFragment();

    @Before
    public void setUp() throws Exception {
        RobolectricApplication application = ApplicationProvider.getApplicationContext();
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
                .geoDependencyModule(new GeoDependencyModule() {
                    @NonNull
                    @Override
                    public MapFragmentFactory providesMapFragmentFactory() {
                        return context -> mapFragment;
                    }

                    @NonNull
                    @Override
                    public ReferenceLayerSettingsNavigator providesReferenceLayerSettingsNavigator() {
                        return activity -> { };
                    }
                })
                .build();
    }

    @Test
    public void shouldReturnPointFromSecondLocationFix() {
        ActivityScenario<GeoPointMapActivity> scenario = ActivityScenario.launch(GeoPointMapActivity.class);

        // The very first fix is ignored.
        mapFragment.setLocationProvider("GPS");
        mapFragment.setLocation(new MapPoint(1, 2, 3, 4f));
        scenario.onActivity(activity -> {
            assertEquals(activity.getString(R.string.please_wait_long), activity.getLocationStatus());
        });


        // The second fix changes the status message.
        mapFragment.setLocation(new MapPoint(5, 6, 7, 8f));
        scenario.onActivity(activity -> {
            assertEquals(activity.formatLocationStatus("gps", 8f), activity.getLocationStatus());
        });

        // When the user clicks the "Save" button, the fix location should be returned.
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.accept_location).performClick();
        });

        assertThat(scenario.getResult().getResultCode(), is(RESULT_OK));
        scenario.onActivity(activity -> {
            assertThat(scenario.getResult().getResultData().getStringExtra("value"), is(activity.formatResult(new MapPoint(5, 6, 7, 8))));
        });
    }

    @Test
    public void mapFragmentRetainMockAccuracy_isFalse() {
        ActivityScenario.launch(GeoPointMapActivity.class);
        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }

    @Test
    public void passingRetainMockAccuracyExtra_showSetItOnLocationClient() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointMapActivity.class);
        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true);
        ActivityScenario.launch(intent);
        assertThat(mapFragment.isRetainMockAccuracy(), is(true));

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, false);
        ActivityScenario.launch(intent);
        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }

    private static class FakeMapFragment implements MapFragment {

        private PointListener pointListener;
        private String locationProvider;
        private boolean retainMockAccuracy;

        @Override
        public void applyConfig(Bundle config) {

        }

        @Override
        public void addTo(@NonNull FragmentActivity activity, int containerId, @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
            readyListener.onReady(this);
        }

        @NonNull
        @Override
        public MapPoint getCenter() {
            return null;
        }

        @Override
        public double getZoom() {
            return 0;
        }

        @Override
        public void setCenter(@Nullable MapPoint center, boolean animate) {

        }

        @Override
        public void zoomToPoint(@Nullable MapPoint center, boolean animate) {

        }

        @Override
        public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {

        }

        @Override
        public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {

        }

        @Override
        public int addMarker(MapPoint point, boolean draggable, String iconAnchor) {
            return 0;
        }

        @Override
        public void setMarkerIcon(int featureId, int drawableId) {

        }

        @Override
        public MapPoint getMarkerPoint(int featureId) {
            return null;
        }

        @Override
        public int addDraggablePoly(@NonNull Iterable<MapPoint> points, boolean closedPolygon) {
            return 0;
        }

        @Override
        public void appendPointToPoly(int featureId, @NonNull MapPoint point) {

        }

        @Override
        public void removePolyLastPoint(int featureId) {

        }

        @NonNull
        @Override
        public List<MapPoint> getPolyPoints(int featureId) {
            return null;
        }

        @Override
        public void removeFeature(int featureId) {

        }

        @Override
        public void clearFeatures() {

        }

        @Override
        public void setClickListener(@Nullable PointListener listener) {

        }

        @Override
        public void setLongPressListener(@Nullable PointListener listener) {

        }

        @Override
        public void setFeatureClickListener(@Nullable FeatureListener listener) {

        }

        @Override
        public void setDragEndListener(@Nullable FeatureListener listener) {

        }

        @Override
        public void setGpsLocationEnabled(boolean enabled) {

        }

        @Nullable
        @Override
        public MapPoint getGpsLocation() {
            return null;
        }

        @Nullable
        @Override
        public String getLocationProvider() {
            return locationProvider;
        }

        @Override
        public void runOnGpsLocationReady(@NonNull ReadyListener listener) {

        }

        @Override
        public void setGpsLocationListener(@Nullable PointListener listener) {
            this.pointListener = listener;
        }

        @Override
        public void setRetainMockAccuracy(boolean retainMockAccuracy) {
            this.retainMockAccuracy = retainMockAccuracy;
        }

        public void setLocation(MapPoint mapPoint) {
            if (pointListener != null) {
                pointListener.onPoint(mapPoint);
            }
        }

        public void setLocationProvider(String locationProvider) {
            this.locationProvider = locationProvider;
        }

        public boolean isRetainMockAccuracy() {
            return retainMockAccuracy;
        }
    }
}
