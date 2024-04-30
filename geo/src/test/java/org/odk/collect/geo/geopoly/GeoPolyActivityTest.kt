package org.odk.collect.geo.geopoly

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.androidtest.ActivityScenarioExtensions.isFinishing
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.geo.Constants
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.R
import org.odk.collect.geo.ReferenceLayerSettingsNavigator
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPolyActivityTest {
    private val mapFragment = FakeMapFragment()
    private val locationTracker = mock<LocationTracker>()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setUp() {
        val shadowApplication = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION")
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION")
        val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesMapFragmentFactory(): MapFragmentFactory {
                    return object : MapFragmentFactory {
                        override fun createMapFragment(): MapFragment {
                            return mapFragment
                        }
                    }
                }

                override fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
                    return object : ReferenceLayerSettingsNavigator {
                        override fun navigateToReferenceLayerSettings(activity: FragmentActivity) {
                        }
                    }
                }

                override fun providesLocationTracker(application: Application): LocationTracker {
                    return locationTracker
                }
            })
            .build()
    }

    @Test
    fun testLocationTrackerLifecycle() {
        val scenario = launcherRule.launch(
            GeoPolyActivity::class.java
        )
        mapFragment.ready()

        // Stopping the activity should stop the location tracker
        scenario.moveToState(Lifecycle.State.DESTROYED)
        Mockito.verify(locationTracker).stop()
    }

    @Test
    fun recordButton_should_beHiddenForAutomaticMode() {
        launcherRule.launch(GeoPolyActivity::class.java)
        mapFragment.ready()
        startInput(R.id.automatic_mode)
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun recordButton_should_beVisibleForManualMode() {
        launcherRule.launch(GeoPolyActivity::class.java)
        mapFragment.ready()
        startInput(R.id.manual_mode)
        onView(withId(R.id.record_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenPolygonExtraPresent_showsPoly() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        val polys = mapFragment.getPolyLines()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points, equalTo(polygon))
    }

    @Test
    fun whenPolygonExtraPresent_andOutputModeIsShape_showsClosedPoly() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        polygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE)
        launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        val polys = mapFragment.getPolyLines()
        assertThat(polys.size, equalTo(1))
        val expectedPolygon = ArrayList<MapPoint>()
        expectedPolygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        expectedPolygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        assertThat(polys[0].points, equalTo(expectedPolygon))
        assertThat(mapFragment.isPolyClosed(0), equalTo(true))
    }

    @Test
    fun whenPolygonExtraPresent_andPolyIsEmpty_andOutputModeIsShape_doesNotShowPoly() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        val polygon = ArrayList<MapPoint>()
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE)
        launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        val polys = mapFragment.getPolyLines()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points.isEmpty(), equalTo(true))
    }

    @Test
    fun whenPolygonExtraPresent_andPolyIsEmpty_pressingBack_finishes() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        val polygon = ArrayList<MapPoint>()
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE)
        val scenario = launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        Espresso.pressBack()
        assertThat(scenario.isFinishing, equalTo(true))
    }

    @Test
    fun startingInput_usingAutomaticMode_usesRetainMockAccuracyTrueToStartLocationTracker() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(Constants.EXTRA_RETAIN_MOCK_ACCURACY, true)
        launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        startInput(R.id.automatic_mode)
        verify(locationTracker).start(true)
    }

    @Test
    fun startingInput_usingAutomaticMode_usesRetainMockAccuracyFalseToStartLocationTracker() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(Constants.EXTRA_RETAIN_MOCK_ACCURACY, false)
        launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        startInput(R.id.automatic_mode)
        verify(locationTracker).start(false)
    }

    @Test
    fun recordingPointManually_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        launcherRule.launch(GeoPolyActivity::class.java)
        mapFragment.ready()
        startInput(R.id.manual_mode)
        mapFragment.setLocation(MapPoint(1.0, 1.0))
        onView(withId(R.id.record_button)).perform(click())
        onView(withId(R.id.record_button)).perform(click())
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun placingPoint_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        launcherRule.launch(GeoPolyActivity::class.java)
        mapFragment.ready()
        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(1.0, 1.0))
        mapFragment.click(MapPoint(1.0, 1.0))
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun buttonsShouldBeEnabledInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polyline)
        val scenario = launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        scenario.onActivity { activity: GeoPolyActivity ->
            assertThat(activity.playButton.isEnabled, equalTo(true))
            assertThat(activity.backspaceButton.isEnabled, equalTo(true))
            assertThat(activity.clearButton.isEnabled, equalTo(true))
            assertThat(activity.saveButton.isEnabled, equalTo(true))
        }
    }

    @Test
    fun buttonsShouldBeDisabledInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        intent.putExtra(Constants.EXTRA_READ_ONLY, true)
        val scenario = launcherRule.launch<GeoPolyActivity>(intent)
        mapFragment.ready()
        scenario.onActivity { activity: GeoPolyActivity ->
            assertThat(activity.playButton.isEnabled, equalTo(false))
            assertThat(activity.backspaceButton.isEnabled, equalTo(false))
            assertThat(activity.clearButton.isEnabled, equalTo(false))
            assertThat(activity.saveButton.isEnabled, equalTo(false))
        }
    }

    @Test
    fun polyShouldBeDraggableInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polyline)
        launcherRule.launch<Activity>(intent)
        mapFragment.ready()
        assertThat(mapFragment.isPolyDraggable(0), equalTo(true))
    }

    @Test
    fun polyShouldNotBeDraggableInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GeoPolyActivity::class.java)
        intent.putExtra(GeoPolyActivity.EXTRA_POLYGON, polygon)
        intent.putExtra(Constants.EXTRA_READ_ONLY, true)
        launcherRule.launch<Activity>(intent)
        mapFragment.ready()
        assertThat(mapFragment.isPolyDraggable(0), equalTo(false))
    }

    private fun startInput(mode: Int) {
        onView(withId(R.id.play)).perform(click())
        onView(withId(mode)).inRoot(isDialog()).perform(click())
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
    }
}
