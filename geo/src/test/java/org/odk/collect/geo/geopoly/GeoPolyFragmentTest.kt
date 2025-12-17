package org.odk.collect.geo.geopoly

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.Assertions
import org.odk.collect.testshared.Assertions.assertNotVisible
import org.odk.collect.testshared.Assertions.assertVisible
import org.odk.collect.testshared.FragmentResultRecorder
import org.odk.collect.testshared.Interactions
import org.odk.collect.webpage.WebPageService
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPolyFragmentTest {
    private val mapFragment = FakeMapFragment()
    private val locationTracker = mock<LocationTracker>()

    @get:Rule
    val fragmentLauncherRule = FragmentScenarioLauncherRule()

    @Before
    fun setUp() {
        val shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
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

                override fun providesLocationTracker(application: Application): LocationTracker {
                    return locationTracker
                }

                override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
                    return mock()
                }

                override fun providesScheduler(): Scheduler {
                    return mock()
                }

                override fun providesSettingsProvider(): SettingsProvider {
                    return InMemSettingsProvider()
                }

                override fun providesWebPageService(): WebPageService {
                    return mock()
                }
            })
            .build()
    }

    @Test
    fun testLocationTrackerLifecycle() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )
        mapFragment.ready()

        // Stopping the activity should stop the location tracker
        scenario.moveToState(Lifecycle.State.DESTROYED)
        Mockito.verify(locationTracker).stop()
    }

    @Test
    fun recordButton_should_beHiddenForAutomaticMode() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )

        mapFragment.ready()
        startInput(R.id.automatic_mode)
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun recordButton_should_beVisibleForManualMode() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )

        mapFragment.ready()
        startInput(R.id.manual_mode)
        onView(withId(R.id.record_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenPolygonExtraPresent_showsPoly() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, polygon)
                }
                .build()
        )

        mapFragment.ready()
        val polys = mapFragment.getPolyLines()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points, equalTo(polygon))
    }

    @Test
    fun whenPolygonExtraPresent_andOutputModeIsShape_showsClosedPoly() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        polygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOSHAPE, false, false, polygon)
                }
                .build()
        )

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
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOSHAPE, false, false, emptyList())
                }
                .build()
        )

        mapFragment.ready()
        val polys = mapFragment.getPolyLines()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points.isEmpty(), equalTo(true))
    }

    @Test
    fun whenPolygonExtraPresent_andPolyIsEmpty_pressingBack_setsCancelledResult() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, emptyList())
                }
                .build()
        )

        mapFragment.ready()

        val resultListener = mock<FragmentResultListener>()
        scenario.onFragment {
            it.parentFragmentManager.setFragmentResultListener(
                GeoPolyFragment.REQUEST_GEOPOLY,
                it,
                resultListener
            )
        }

        Espresso.pressBack()
        verify(resultListener).onFragmentResult(GeoPolyFragment.REQUEST_GEOPOLY, Bundle.EMPTY)
    }

    @Test
    fun startingInput_usingAutomaticMode_usesRetainMockAccuracyTrueToStartLocationTracker() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, true, emptyList())
                }
                .build()
        )

        mapFragment.ready()
        startInput(R.id.automatic_mode)
        verify(locationTracker).start(true)
    }

    @Test
    fun startingInput_usingAutomaticMode_usesRetainMockAccuracyFalseToStartLocationTracker() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, emptyList())
                }
                .build()
        )

        mapFragment.ready()
        startInput(R.id.automatic_mode)
        verify(locationTracker).start(false)
    }

    @Test
    fun recordingPointManually_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )

        mapFragment.ready()
        startInput(R.id.manual_mode)
        mapFragment.setLocation(MapPoint(1.0, 1.0))
        onView(withId(R.id.record_button)).perform(click())
        onView(withId(R.id.record_button)).perform(click())
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun placingPoint_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )

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
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, polyline)
                }
                .build()
        )

        mapFragment.ready()
        Assertions.assertEnabled(withContentDescription(string.input_method))
        Assertions.assertEnabled(withContentDescription(string.remove_last_point))
        Assertions.assertEnabled(withContentDescription(string.clear))
        Assertions.assertEnabled(withContentDescription(string.save))
    }

    @Test
    fun buttonsShouldBeDisabledInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, true, false, polygon)
                }
                .build()
        )

        mapFragment.ready()
        Assertions.assertDisabled(withContentDescription(string.input_method))
        Assertions.assertDisabled(withContentDescription(string.remove_last_point))
        Assertions.assertDisabled(withContentDescription(string.clear))
        Assertions.assertDisabled(withContentDescription(string.save))
    }

    @Test
    fun polyShouldBeDraggableInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, polyline)
                }
                .build()
        )

        mapFragment.ready()
        assertThat(mapFragment.isPolyDraggable(0), equalTo(true))
    }

    @Test
    fun polyShouldNotBeDraggableInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, true, false, polygon)
                }
                .build()
        )

        mapFragment.ready()
        assertThat(mapFragment.isPolyDraggable(0), equalTo(false))
    }

    @Test
    fun passingRetainMockAccuracyExtra_updatesMapFragmentState() {
        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, true, emptyList())
                }
                .build()
        )
        mapFragment.ready()
        assertThat(mapFragment.isRetainMockAccuracy(), equalTo(true))

        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(OutputMode.GEOTRACE, false, false, emptyList())
                }
                .build()
        )
        mapFragment.ready()
        assertThat(mapFragment.isRetainMockAccuracy(), equalTo(false))
    }

    @Test
    fun recreatingTheFragmentWithTheLayersDialogDisplayedDoesNotCrashTheApp() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )
        mapFragment.ready()

        onView(withId(R.id.layers)).perform(click())

        scenario.recreate()
    }

    @Test
    fun showsAndHidesInvalidMessageSnackbarBasedOnValue() {
        val invalidMessage = MutableLiveData<String?>(null)

        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(invalidMessage = invalidMessage) }
                .build()
        )

        val message = "Something is wrong"
        invalidMessage.value = message
        assertVisible(withText(message))

        invalidMessage.value = null
        assertNotVisible(withText(message))
    }

    @Test
    fun whenOutputModeIsGeoTrace_setsChangeResultWheneverAPointIsAddedAfterTheFirst() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOTRACE) }
                .build()
        )

        val resultListener = FragmentResultRecorder()
        scenario.onFragment {
            it.parentFragmentManager.setFragmentResultListener(
                GeoPolyFragment.REQUEST_GEOPOLY,
                it,
                resultListener
            )
        }

        mapFragment.ready()

        startInput(R.id.placement_mode)

        mapFragment.click(MapPoint(0.0, 0.0))
        assertThat(resultListener.result, equalTo(null))

        mapFragment.click(MapPoint(1.0, 1.0))
        val result = resultListener.result
        assertThat(result!!.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun whenOutputModeIsGeoShape_doesNotSetChangeResultUntilThereAre3Points() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) { GeoPolyFragment(OutputMode.GEOSHAPE) }
                .build()
        )

        val resultListener = FragmentResultRecorder()
        scenario.onFragment {
            it.parentFragmentManager.setFragmentResultListener(
                GeoPolyFragment.REQUEST_GEOPOLY,
                it,
                resultListener
            )
        }

        mapFragment.ready()

        startInput(R.id.placement_mode)

        mapFragment.click(MapPoint(0.0, 0.0))
        assertThat(resultListener.result, equalTo(null))

        mapFragment.click(MapPoint(1.0, 0.0))
        assertThat(resultListener.result, equalTo(null))

        mapFragment.click(MapPoint(1.0, 1.0))
        val result = resultListener.result
        assertThat(result!!.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0;0.0 0.0 0.0 0.0")
        )
    }

    @Test
    fun setsChangeResultWheneverAPointIsRemoved() {
        val scenario = fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(
                        inputPolygon =
                            listOf(
                                MapPoint(0.0, 0.0),
                                MapPoint(1.0, 0.0),
                                MapPoint(1.0, 1.0)
                            )
                    )
                }
                .build()
        )

        val resultListener = FragmentResultRecorder()
        scenario.onFragment {
            it.parentFragmentManager.setFragmentResultListener(
                GeoPolyFragment.REQUEST_GEOPOLY,
                it,
                resultListener
            )
        }

        mapFragment.ready()

        Interactions.clickOn(withContentDescription(string.remove_last_point))
        val result = resultListener.result
        assertThat(result!!.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0")
        )
    }

    private fun startInput(mode: Int) {
        onView(withId(R.id.play)).perform(click())
        onView(withId(mode)).inRoot(isDialog()).perform(click())
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
    }
}
