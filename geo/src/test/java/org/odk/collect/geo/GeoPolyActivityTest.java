/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.geo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static java.util.Collections.emptyList;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapFragmentFactory;
import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.location.tracker.LocationTracker;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GeoPolyActivityTest {

    private final FakeMapFragment mapFragment = new FakeMapFragment();
    private final LocationTracker locationTracker = mock(LocationTracker.class);

    @Before
    public void setUp() throws Exception {
        RobolectricApplication application = ApplicationProvider.getApplicationContext();
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
                .geoDependencyModule(new GeoDependencyModule() {
                    @NonNull
                    @Override
                    public MapFragmentFactory providesMapFragmentFactory() {
                        return (context) -> mapFragment;
                    }

                    @NonNull
                    @Override
                    public ReferenceLayerSettingsNavigator providesReferenceLayerSettingsNavigator() {
                        return (activity) -> {};
                    }

                    @NonNull
                    @Override
                    public LocationTracker providesLocationTracker() {
                        return locationTracker;
                    }
                })
                .build();
    }

    @Test
    public void testLocationTrackerLifecycle() {
        ActivityScenario<GeoPolyActivity> scenario = ActivityScenario.launch(GeoPolyActivity.class);

        // Stopping the activity should stop the location tracker
        scenario.moveToState(Lifecycle.State.DESTROYED);
        verify(locationTracker).stop();
    }

    @Test
    public void recordButton_should_beHiddenForAutomaticMode() {
        ActivityScenario<GeoPolyActivity> scenario = ActivityScenario.launch(GeoPolyActivity.class);

        scenario.onActivity((activity -> {
            activity.updateRecordingMode(R.id.automatic_mode);
            activity.startInput();
            assertThat(activity.findViewById(R.id.record_button).getVisibility(), is(View.GONE));
        }));
    }

    @Test
    public void recordButton_should_beVisibleForManualMode() {
        ActivityScenario<GeoPolyActivity> scenario = ActivityScenario.launch(GeoPolyActivity.class);

        scenario.onActivity((activity -> {
            activity.updateRecordingMode(R.id.manual_mode);
            activity.startInput();
            assertThat(activity.findViewById(R.id.record_button).getVisibility(), is(View.VISIBLE));
        }));
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
            return emptyList();
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
