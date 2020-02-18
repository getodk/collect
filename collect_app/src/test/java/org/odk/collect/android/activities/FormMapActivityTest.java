package org.odk.collect.android.activities;

import android.app.Fragment;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.geo.TestMapFragment;
import org.odk.collect.android.instances.TestInstancesRepository;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.MapsPreferences;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.activities.FormMapViewModelTest.testInstances;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class FormMapActivityTest {
    private ActivityController activityController;
    private FormMapActivity activity;

    private final List<MapPoint> expectedPoints = Arrays.asList(new MapPoint(10.0, 125.6),
            new MapPoint(10.1, 125.6), new MapPoint(10.1, 126.6),
            new MapPoint(10.3, 125.6), new MapPoint(10.3, 125.7),
            new MapPoint(10.4, 125.6));
    private final MapPoint currentLocation = new MapPoint(5, 5);

    @Before public void setUpActivity() {
        activityController = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        activity = (FormMapActivity) activityController.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(Arrays.asList(testInstances));
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);

        activity.map = new TestMapFragment();

        activityController.setup();
    }

    @Test public void startingFormMap_zoomsToFitAllInstanceMarkers_ifThereAreInstanceMarkers() {
        TestMapFragment map = (TestMapFragment) activity.map;
        assertThat(map.getZoomCount(), is(1));
        assertThat(map.getLatestZoomPoint(), is(nullValue()));
        assertThat(map.getLatestZoomBoundingBox(), is(expectedPoints));
        assertThat(map.getLatestScaleFactor(), is(0.8));
        assertThat(map.wasLatestZoomCallAnimated(), is(false));
    }

    @Test public void startingFormMap_doesNotZoom_ifThereAreNoInstanceMarkers_andLocationIsUnavailable() {
        ActivityController controller = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        FormMapActivity activity = (FormMapActivity) controller.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(new ArrayList<>());
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);
        activity.map = new TestMapFragment();

        controller.setup();

        TestMapFragment map = (TestMapFragment) activity.map;
        assertThat(map.getZoomCount(), is(0));
    }

    @Test public void locationChange_zoomsToCurrentLocation_ifTheViewportWasNotPreviouslyUpdated() {
        ActivityController controller = RobolectricHelpers.buildThemedActivity(FormMapActivity.class);
        FormMapActivity activity = (FormMapActivity) controller.get();

        TestInstancesRepository testInstancesRepository = new TestInstancesRepository(new ArrayList<>());
        FormMapViewModel viewModel = new FormMapViewModel(FormMapViewModelTest.TEST_FORM_1, testInstancesRepository);
        activity.viewModelFactory = new TestFactory(viewModel);
        activity.map = new TestMapFragment();

        controller.setup();

        TestMapFragment map = (TestMapFragment) activity.map;
        assertThat(map.getZoomCount(), is(0));

        map.onLocationChanged(currentLocation);

        assertThat(map.getZoomCount(), is(1));
        assertThat(map.getLatestZoomPoint(), is(currentLocation));
        assertThat(map.wasLatestZoomCallAnimated(), is(true));
    }

    @Test public void tappingOnZoomToCurrentLocationButton_zoomsToCurrentLocationWithAnimation() {
        activity.findViewById(R.id.zoom_to_location).performClick();

        TestMapFragment map = (TestMapFragment) activity.map;
        assertThat(map.getZoomCount(), is(2)); // once on initialization and once on click
        assertThat(map.getLatestZoomPoint(), is(currentLocation));
        assertThat(map.wasLatestZoomCallAnimated(), is(true));
    }

    @Test public void tappingOnZoomToFitButton_zoomsToFitAllInstanceMarkersWithoutAnimation() {
        activity.findViewById(R.id.zoom_to_bounds).performClick();

        TestMapFragment map = (TestMapFragment) activity.map;
        assertThat(map.getZoomCount(), is(2));
        assertThat(map.getLatestZoomPoint(), is(nullValue()));
        assertThat(map.getLatestZoomBoundingBox(), is(expectedPoints));
        assertThat(map.getLatestScaleFactor(), is(0.8));
        assertThat(map.wasLatestZoomCallAnimated(), is(false));
    }

    @Test public void tappingOnLayerMenu_opensLayerDialog() {
        List<Fragment> fragments = activity.getFragmentManager().getFragments();
        assertThat(fragments, not(hasItem(isA(MapsPreferences.class))));

        activity.findViewById(R.id.layer_menu).performClick();

        fragments = activity.getFragmentManager().getFragments();
        assertThat(fragments, hasItem(isA(MapsPreferences.class)));
    }

    @Test public void tappingOnNewInstanceButton_opensNewInstance() {
        activity.findViewById(R.id.new_instance).performClick();

        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

        assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
        assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(nullValue()));
    }

    @Ignore("Doesn't work with field-based dependency injection because we don't get an opportunity" +
            "to set test doubles before onCreate() is called after the orientation change")
    @Test public void centerAndZoomLevel_areRestoredAfterOrientationChange() {
        activity.map.zoomToPoint(new MapPoint(7, 7), 7, false);

        RuntimeEnvironment.setQualifiers("+land");
        activityController.configurationChange();

        assertThat(activity.map.getCenter(), is(new MapPoint(7, 7)));
        assertThat(activity.map.getZoom(), is(7));
    }

    // Note that there's a point with deleted status included. This shouldn't be possible in real
    // usage because deleting a form removes the geometry from the database. However, the database
    // allows a deleted instance with geometry so we test it.
    @Test public void mappedPoints_matchInstancesWithGeometry() {
        TestMapFragment map = (TestMapFragment) activity.map;

        assertThat(map.getMappedPointCount(), is(expectedPoints.size()));
        for (MapPoint expectedPoint : expectedPoints) {
            assertThat(map.isMapped(expectedPoint), is(true));
        }
    }

    @Test public void tappingOnEditableInstances_launchesEditActivity() {
        TestMapFragment map = (TestMapFragment) activity.map;

        MapPoint editableAndFinalized = new MapPoint(10.1, 125.6);
        MapPoint unfinalized = new MapPoint(10.1, 126.6);
        MapPoint failedToSend = new MapPoint(10.3, 125.6);

        MapPoint[] testPoints = {editableAndFinalized, unfinalized, failedToSend};

        for (MapPoint toTap : testPoints) {
            int featureId = map.getFeatureIdFor(toTap);

            activity.onFeatureClicked(featureId);
            Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

            assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
            assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(nullValue()));
        }
    }

    @Test public void tappingOnEditableInstance_whenEditingSettingisOff_launchesViewActivity() {
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_EDIT_SAVED, false);

        TestMapFragment map = (TestMapFragment) activity.map;

        MapPoint editableAndFinalized = new MapPoint(10.1, 125.6);
        MapPoint unfinalized = new MapPoint(10.1, 126.6);
        MapPoint failedToSend = new MapPoint(10.3, 125.6);

        MapPoint[] testPoints = {editableAndFinalized, unfinalized, failedToSend};

        for (MapPoint toTap : testPoints) {
            int featureId = map.getFeatureIdFor(toTap);

            activity.onFeatureClicked(featureId);
            Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

            assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
            assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(ApplicationConstants.FormModes.VIEW_SENT));
        }
    }

    @Test public void tappingOnUneditableInstances_launchesViewActivity() {
        TestMapFragment map = (TestMapFragment) activity.map;
        MapPoint sent = new MapPoint(10.3, 125.7);

        int featureId = map.getFeatureIdFor(sent);

        activity.onFeatureClicked(featureId);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

        assertThat(actual.getAction(), is(Intent.ACTION_EDIT));
        assertThat(actual.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE), is(ApplicationConstants.FormModes.VIEW_SENT));
    }

    // Geometry is removed from the database on instance encryption but just in case there is an
    // encrypted instance with geometry available, show an encrypted toast.
    @Test public void tappingOnEncryptedInstances_showsUneditableToast() {
        TestMapFragment map = (TestMapFragment) activity.map;
        MapPoint submissionFailedCantEditWhenFinalized = new MapPoint(10.4, 125.6);

        int featureId = map.getFeatureIdFor(submissionFailedCantEditWhenFinalized);

        activity.onFeatureClicked(featureId);
        assertThat(ShadowToast.getTextOfLatestToast(), is("This form cannot be edited once it has been marked as finalized. It may be encrypted."));
    }

    // Geometry is removed from the database on instance deletion but just in case there is a
    // deleted instance with geometry available, show a deleted toast.
    @Test public void tappingOnDeletedInstances_showsDeletedToast() {
        TestMapFragment map = (TestMapFragment) activity.map;
        MapPoint deleted = new MapPoint(10.0, 125.6);

        int featureId = map.getFeatureIdFor(deleted);

        activity.onFeatureClicked(featureId);
        assertThat(ShadowToast.getTextOfLatestToast(), containsString("Deleted on"));
    }

    private static class TestFactory implements ViewModelProvider.Factory {

        private final FormMapViewModel viewModel;

        TestFactory(FormMapViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) viewModel;
        }
    }
}
