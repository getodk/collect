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

package org.odk.collect.geo.geopoly;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.androidtest.ActivityScenarioLauncherRule;
import org.odk.collect.geo.Constants;
import org.odk.collect.geo.DaggerGeoDependencyComponent;
import org.odk.collect.geo.GeoDependencyModule;
import org.odk.collect.geo.R;
import org.odk.collect.geo.ReferenceLayerSettingsNavigator;
import org.odk.collect.geo.support.FakeMapFragment;
import org.odk.collect.geo.support.RobolectricApplication;
import org.odk.collect.location.tracker.LocationTracker;
import org.odk.collect.maps.MapFragmentFactory;
import org.odk.collect.maps.MapPoint;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AndroidJUnit4.class)
public class GeoPolyActivityTest {

    private final FakeMapFragment mapFragment = new FakeMapFragment();
    private final LocationTracker locationTracker = mock(LocationTracker.class);

    @Rule
    public ActivityScenarioLauncherRule launcherRule = new ActivityScenarioLauncherRule();

    @Before
    public void setUp() {
        ShadowApplication shadowApplication = shadowOf(ApplicationProvider.<Application>getApplicationContext());
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION");

        RobolectricApplication application = ApplicationProvider.getApplicationContext();
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
                .application(application)
                .geoDependencyModule(new GeoDependencyModule() {
                    @NonNull
                    @Override
                    public MapFragmentFactory providesMapFragmentFactory() {
                        return () -> mapFragment;
                    }

                    @NonNull
                    @Override
                    public ReferenceLayerSettingsNavigator providesReferenceLayerSettingsNavigator() {
                        return (activity) -> {};
                    }

                    @NonNull
                    @Override
                    public LocationTracker providesLocationTracker(@NonNull Application application) {
                        return locationTracker;
                    }
                })
                .build();
    }

    @Test
    public void testLocationTrackerLifecycle() {
        ActivityScenario<GeoPolyActivity> scenario = launcherRule.launch(GeoPolyActivity.class);
        mapFragment.ready();

        // Stopping the activity should stop the location tracker
        scenario.moveToState(Lifecycle.State.DESTROYED);
        verify(locationTracker).stop();
    }

    @Test
    public void recordButton_should_beHiddenForAutomaticMode() {
        launcherRule.launch(GeoPolyActivity.class);
        mapFragment.ready();

        startInput(R.id.automatic_mode);
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void recordButton_should_beVisibleForManualMode() {
        launcherRule.launch(GeoPolyActivity.class);
        mapFragment.ready();

        startInput(R.id.manual_mode);
        onView(withId(R.id.record_button)).check(matches(isDisplayed()));
    }

    @Test
    public void startingInput_usingAutomaticMode_usesRetainMockAccuracyTrueToStartLocationTracker() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity.class);

        intent.putExtra(Constants.EXTRA_RETAIN_MOCK_ACCURACY, true);
        launcherRule.<GeoPolyActivity>launch(intent);

        mapFragment.ready();
        startInput(R.id.automatic_mode);
        verify(locationTracker).start(true);
    }

    @Test
    public void startingInput_usingAutomaticMode_usesRetainMockAccuracyFalseToStartLocationTracker() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity.class);

        intent.putExtra(Constants.EXTRA_RETAIN_MOCK_ACCURACY, false);
        launcherRule.<GeoPolyActivity>launch(intent);

        mapFragment.ready();
        startInput(R.id.automatic_mode);
        verify(locationTracker).start(false);
    }

    @Test
    public void recordingPointManually_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        launcherRule.launch(GeoPolyActivity.class);
        mapFragment.ready();

        startInput(R.id.manual_mode);

        mapFragment.setLocation(new MapPoint(1, 1));
        onView(withId(R.id.record_button)).perform(click());
        onView(withId(R.id.record_button)).perform(click());
        assertThat(mapFragment.getPolyPoints(0).size(), equalTo(1));
    }

    @Test
    public void placingPoint_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        launcherRule.launch(GeoPolyActivity.class);
        mapFragment.ready();

        startInput(R.id.placement_mode);

        mapFragment.click(new MapPoint(1, 1));
        mapFragment.click(new MapPoint(1, 1));
        assertThat(mapFragment.getPolyPoints(0).size(), equalTo(1));
    }

    private void startInput(int mode) {
        onView(withId(R.id.play)).perform(click());
        onView(withId(mode)).inRoot(isDialog()).perform(click());
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
    }
}
