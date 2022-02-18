package org.odk.collect.geo;

import static android.app.Activity.RESULT_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.androidtest.ActivityScenarioLauncherRule;
import org.odk.collect.externalapp.ExternalAppUtils;
import org.odk.collect.geo.maps.MapFragmentFactory;
import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.geo.support.FakeMapFragment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AndroidJUnit4.class)
public class GeoPointMapActivityTest {

    private final FakeMapFragment mapFragment = new FakeMapFragment();

    @Rule
    public ActivityScenarioLauncherRule launcherRule = new ActivityScenarioLauncherRule();

    @Before
    public void setUp() throws Exception {
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
        ActivityScenario<GeoPointMapActivity> scenario = launcherRule.launch(GeoPointMapActivity.class);

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
            Intent resultData = scenario.getResult().getResultData();
            assertThat(ExternalAppUtils.getReturnedSingleValue(resultData), is(activity.formatResult(new MapPoint(5, 6, 7, 8))));
        });
    }

    @Test
    public void mapFragmentRetainMockAccuracy_isFalse() {
        launcherRule.launch(GeoPointMapActivity.class);
        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }

    @Test
    public void passingRetainMockAccuracyExtra_showSetItOnLocationClient() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointMapActivity.class);
        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true);
        launcherRule.launch(intent);
        assertThat(mapFragment.isRetainMockAccuracy(), is(true));

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, false);
        launcherRule.launch(intent);
        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }
}
