package org.odk.collect.geo.geopoint;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.androidtest.ActivityScenarioLauncherRule;
import org.odk.collect.async.Scheduler;
import org.odk.collect.externalapp.ExternalAppUtils;
import org.odk.collect.geo.DaggerGeoDependencyComponent;
import org.odk.collect.geo.GeoDependencyModule;
import org.odk.collect.geo.R;
import org.odk.collect.geo.support.FakeMapFragment;
import org.odk.collect.geo.support.RobolectricApplication;
import org.odk.collect.maps.MapFragmentFactory;
import org.odk.collect.maps.MapPoint;
import org.odk.collect.maps.layers.ReferenceLayerRepository;
import org.odk.collect.settings.InMemSettingsProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.webpage.ExternalWebPageHelper;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GeoPointMapActivityTest {

    private final FakeMapFragment mapFragment = new FakeMapFragment();

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
                    public ReferenceLayerRepository providesReferenceLayerRepository() {
                        return mock();
                    }

                    @NonNull
                    @Override
                    public Scheduler providesScheduler() {
                        return mock();
                    }

                    @NonNull
                    @Override
                    public SettingsProvider providesSettingsProvider() {
                        return new InMemSettingsProvider();
                    }

                    @NonNull
                    @Override
                    public ExternalWebPageHelper providesExternalWebPageHelper() {
                        return mock();
                    }
                })
                .build();
    }

    @Test
    public void whenLocationNotSetShouldDisplayPleaseWaitMessage() {
        ActivityScenario<GeoPointMapActivity> scenario = launcherRule.launchForResult(GeoPointMapActivity.class);
        mapFragment.ready();

        scenario.onActivity(activity -> assertEquals(activity.getString(org.odk.collect.strings.R.string.please_wait_long), getLocationStatus(activity)));
    }

    @Test
    public void whenLocationSetShouldDisplayStatusMessage() {
        ActivityScenario<GeoPointMapActivity> scenario = launcherRule.launchForResult(GeoPointMapActivity.class);
        mapFragment.ready();
        mapFragment.setLocationProvider("GPS");
        mapFragment.setLocation(new MapPoint(1, 2, 3, 4f));

        scenario.onActivity(activity -> assertEquals("Accuracy: 4 m", getLocationStatus(activity)));
    }

    @Test
    public void shouldReturnPointFromLastLocationFix() {
        ActivityScenario<GeoPointMapActivity> scenario = launcherRule.launchForResult(GeoPointMapActivity.class);
        mapFragment.ready();
        mapFragment.setLocationProvider("GPS");

        // First location
        mapFragment.setLocation(new MapPoint(1, 2, 3, 4f));

        // Second location
        mapFragment.setLocation(new MapPoint(5, 6, 7, 8f));

        // When the user clicks the "Save" button, the fix location should be returned.
        scenario.onActivity(activity -> activity.findViewById(R.id.accept_location).performClick());

        assertThat(scenario.getResult().getResultCode(), is(RESULT_OK));
        scenario.onActivity(activity -> {
            Intent resultData = scenario.getResult().getResultData();
            assertThat(ExternalAppUtils.getReturnedSingleValue(resultData), is(activity.formatResult(new MapPoint(5, 6, 7, 8))));
        });
    }

    @Test
    public void whenLocationExtraIncluded_showsMarker() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointMapActivity.class);
        intent.putExtra(GeoPointMapActivity.EXTRA_LOCATION, new MapPoint(1.0, 2.0));
        launcherRule.launch(intent);
        mapFragment.ready();

        List<MapPoint> markers = mapFragment.getMarkers();
        assertThat(markers.size(), equalTo(1));
        assertThat(markers.get(0).latitude, equalTo(1.0));
        assertThat(markers.get(0).longitude, equalTo(2.0));
    }

    @Test
    public void mapFragmentRetainMockAccuracy_isFalse() {
        launcherRule.launch(GeoPointMapActivity.class);
        mapFragment.ready();

        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }

    @Test
    public void passingRetainMockAccuracyExtra_updatesMapFragmentState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GeoPointMapActivity.class);
        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true);
        launcherRule.launch(intent);
        mapFragment.ready();

        assertThat(mapFragment.isRetainMockAccuracy(), is(true));

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, false);
        launcherRule.launch(intent);
        mapFragment.ready();

        assertThat(mapFragment.isRetainMockAccuracy(), is(false));
    }

    @Test
    public void recreatingTheActivityWithTheLayersDialogDisplayedDoesNotCrashTheApp() {
        ActivityScenario<GeoPointMapActivity> scenario = launcherRule.launch(GeoPointMapActivity.class);
        mapFragment.ready();

        onView(withId(R.id.layer_menu)).perform(click());

        scenario.recreate();
    }

    private String getLocationStatus(Activity activity) {
        return activity
                .findViewById(R.id.status_section)
                .<TextView>findViewById(R.id.location_status)
                .getText().toString();
    }
}
