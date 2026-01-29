package org.odk.collect.geo.geopoly

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.utils.opaque
import org.odk.collect.androidtest.FragmentScenarioExtensions.setFragmentResultListener
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoly.GeoPolyFragment.Companion.INTERVAL_OPTIONS
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.geo.support.FakeLocationTracker
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapConsts
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
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.FragmentResultRecorder
import org.odk.collect.testshared.Interactions
import org.odk.collect.webpage.WebPageService
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPolyFragmentTest {
    private val mapFragment = FakeMapFragment(ready = true)

    private val locationTracker = FakeLocationTracker()

    @get:Rule
    val fragmentLauncherRule = FragmentScenarioLauncherRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val scheduler = FakeScheduler()

    @Before
    fun setUp() {
        val shadowApplication =
            Shadows.shadowOf(application)
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
                    return scheduler
                }

                override fun providesSettingsProvider(): SettingsProvider {
                    return InMemSettingsProvider()
                }

                override fun providesWebPageService(): WebPageService {
                    return mock()
                }
            })
            .build()

        SnackbarUtils.alertStore.enabled = true
    }

    @After
    fun teardown() {
        SnackbarUtils.alertStore.enabled = false
    }

    @Test
    fun testLocationTrackerLifecycle() {
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        // Stopping the activity should stop the location tracker
        scenario.moveToState(Lifecycle.State.DESTROYED)
        assertThat(locationTracker.isStarted, equalTo(false))
    }

    @Test
    fun recordButton_should_beHiddenForAutomaticMode() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun recordButton_should_beVisibleForManualMode() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.manual_mode)
        onView(withId(R.id.record_button)).check(matches(isDisplayed()))
    }

    @Test
    fun placingPoint_updatesCollectionStatus() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(1.0, 1.0))
        assertVisible(withText(application.getString(string.collection_status_placement, 1)))
    }

    @Test
    fun recordingPointsAutomatically_updatesCollectionStatus() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 0, 20, 10)
            )
        )

        locationTracker.currentLocation = Location(1.0, 1.0)
        scheduler.runForeground(0)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 1, 20, 10)
            )
        )

        locationTracker.currentLocation = Location(2.0, 2.0)
        scheduler.runForeground(DEFAULT_RECORDING_INTERVAL)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 2, 20, 10)
            )
        )
    }

    @Test
    fun recordingPointsAutomatically_doesNotRecordFasterThanInterval() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        locationTracker.currentLocation = Location(1.0, 1.0)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 0, 20, 10)
            )
        )
    }

    @Test
    fun recordingPointsAutomatically_andClickingPause_stopsRecording() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 0, 20, 10)
            )
        )

        locationTracker.currentLocation = Location(1.0, 1.0)
        scheduler.runForeground(0)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 1, 20, 10)
            )
        )

        Interactions.clickOn(withContentDescription(string.pause_location_recording))
        locationTracker.currentLocation = Location(2.0, 2.0)
        scheduler.runForeground(DEFAULT_RECORDING_INTERVAL)
        assertVisible(
            withText(
                application.getString(string.collection_status_paused, 1)
            )
        )
    }

    @Test
    fun whenPolygonExtraPresent_showsPoly() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = polygon)
        }

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
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE,
                inputPolygon = polygon
            )
        }

        val polys = mapFragment.getPolygons()
        assertThat(polys.size, equalTo(1))
        val expectedPolygon = ArrayList<MapPoint>()
        expectedPolygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        expectedPolygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        assertThat(polys[0].points, equalTo(expectedPolygon))
    }

    @Test
    fun whenPolygonExtraPresent_andPolyIsEmpty_andOutputModeIsShape_doesNotShowPoly() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE,
                inputPolygon = emptyList()
            )
        }

        val polys = mapFragment.getPolygons()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points.isEmpty(), equalTo(true))
    }

    @Test
    fun pressingBack_setsCancelledResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ onBackPressedDispatcher })
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        onBackPressedDispatcher.onBackPressed()
        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(result.second.isEmpty, equalTo(true))
    }

    @Test
    fun whenInputPolyIsNotEmpty_pressingBack_setsCancelledResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(1.0, 1.0))
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        onBackPressedDispatcher.onBackPressed()
        val result = resultListener.getAll().lastOrNull()
        assertThat(result!!.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(result.second.isEmpty, equalTo(true))
    }

    @Test
    fun whenPolygonHasBeenModified_pressingBack_andClickingCancel_setsNoResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ onBackPressedDispatcher })
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(1.0, 1.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        Interactions.clickOn(withText(string.cancel), root = isDialog())

        val result = resultListener.getAll().lastOrNull()
        assertThat(result, equalTo(null))
    }

    @Test
    fun whenPolygonHasBeenCreated_pressingBack_andClickingDiscard_setsEmptyResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ onBackPressedDispatcher })
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(1.0, 1.0))
        mapFragment.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        Interactions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(result.second.getString(GeoPolyFragment.RESULT_GEOPOLY), equalTo(""))
    }

    @Test
    fun whenPolygonHasBeenModified_pressingBack_andClickingDiscard_setsOriginalAsResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0))
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        mapFragment.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        Interactions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun whenInputPolygonIsOnlyOnePoint_andHasBeenModified_pressingBack_andClickingDiscard_setsOriginalAsResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0))
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        mapFragment.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        Interactions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY),
            equalTo("0.0 0.0 0.0 0.0")
        )
    }

    @Test
    fun whenPolygonHasBeenModified_recreating_andPressingBack_andClickingDiscard_setsOriginalAsResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0))
            )
        }
        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        mapFragment.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        scenario.recreate()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        onBackPressedDispatcher.onBackPressed()
        Interactions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun recordingPointsAutomatically_usesRetainMockAccuracyTrueToStartLocationTracker() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                true,
                emptyList()
            )
        }

        startInput(R.id.automatic_mode)
        assertThat(locationTracker.retainMockAccuracy, equalTo(true))
    }

    @Test
    fun recordingPointsAutomatically_usesRetainMockAccuracyFalseToStartLocationTracker() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                false,
                emptyList()
            )
        }

        startInput(R.id.automatic_mode)
        assertThat(locationTracker.retainMockAccuracy, equalTo(false))
    }

    @Test
    fun recordingPointManually_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        fragmentLauncherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.manual_mode)
        mapFragment.setLocation(MapPoint(1.0, 1.0))
        onView(withId(R.id.record_button)).perform(click())
        onView(withId(R.id.record_button)).perform(click())
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun placingPoint_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        fragmentLauncherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(1.0, 1.0))
        mapFragment.click(MapPoint(1.0, 1.0))
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun buttonsShouldBeEnabledInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                false,
                polyline
            )
        }

        Assertions.assertEnabled(withContentDescription(string.input_method))
        Assertions.assertEnabled(withContentDescription(string.remove_last_point))
        Assertions.assertEnabled(withContentDescription(string.clear))
        Assertions.assertEnabled(withContentDescription(string.save))
    }

    @Test
    fun buttonsShouldBeDisabledInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                true,
                false,
                polygon
            )
        }

        Assertions.assertDisabled(withContentDescription(string.input_method))
        Assertions.assertDisabled(withContentDescription(string.remove_last_point))
        Assertions.assertDisabled(withContentDescription(string.clear))
        Assertions.assertDisabled(withContentDescription(string.save))
    }

    @Test
    fun polyShouldBeDraggableInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                false,
                polyline
            )
        }

        assertThat(mapFragment.isPolyDraggable(0), equalTo(true))
    }

    @Test
    fun polyShouldNotBeDraggableInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                true,
                false,
                polygon
            )
        }

        assertThat(mapFragment.isPolyDraggable(0), equalTo(false))
    }

    @Test
    fun passingRetainMockAccuracyExtra_updatesMapFragmentState() {
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                true,
                emptyList()
            )
        }

        assertThat(mapFragment.isRetainMockAccuracy(), equalTo(true))

        fragmentLauncherRule.launchInContainer(
            GeoPolyFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(GeoPolyFragment::class) {
                    GeoPolyFragment(
                        { OnBackPressedDispatcher() },
                        OutputMode.GEOTRACE,
                        false,
                        false,
                        emptyList()
                    )
                }
                .build()
        )

        assertThat(mapFragment.isRetainMockAccuracy(), equalTo(false))
    }

    @Test
    fun recreatingTheFragmentWithTheLayersDialogDisplayedDoesNotCrashTheApp() {
        val scenario =
            fragmentLauncherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        onView(withId(R.id.layers)).perform(click())

        scenario.recreate()
    }

    @Test
    fun whenInvalidMessageIsNotNull_pointsCannotBeAddedByClicking() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)

        invalidMessage.value = "Blah"
        mapFragment.click(MapPoint(0.0, 0.0))
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(0))
    }

    @Test
    fun whenInvalidMessageIsNotNull_pointsCannotBeAddedManually() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.manual_mode)

        invalidMessage.value = "Blah"
        Interactions.clickOn(withContentDescription(string.record_geopoint))
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(0))
    }

    @Test
    fun whenInvalidMessageIsNotNull_automaticRecordingStops() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.automatic_mode)

        invalidMessage.value = "Blah"
        locationTracker.currentLocation = Location(1.0, 1.0, 1.0, 1f)
        scheduler.runForeground(0)
        assertThat(mapFragment.getPolyLines()[0].points.size, equalTo(0))
    }

    @Test
    fun showsAndHidesInvalidMessageSnackbarBasedOnValue() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        val message = "Something is wrong"
        invalidMessage.value = message
        assertVisible(withText(message))

        invalidMessage.value = null
        assertNotVisible(withText(message))
        Assertions.assertAlert(
            SnackbarUtils.alertStore,
            application.getString(string.error_fixed),
            "No error fixed message shown!"
        )
    }

    @Test
    fun invalidSnackbarCanBeDismissed() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        val message = "Something is wrong"
        invalidMessage.value = message
        Interactions.clickOn(withContentDescription(string.close_snackbar))
        assertNotVisible(withText(message))
    }

    @Test
    fun changesPolyLineColorBasedOnInvalidMessage() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(0.0, 0.0))

        invalidMessage.value = "blah"
        val errorPolyLine = mapFragment.getPolyLines()[0]
        assertThat(errorPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_ERROR_COLOR))
        assertThat(errorPolyLine.highlightLastPoint, equalTo(false))

        invalidMessage.value = null
        val normalPolyLine = mapFragment.getPolyLines()[0]
        assertThat(normalPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
        assertThat(normalPolyLine.highlightLastPoint, equalTo(true))
    }

    @Test
    fun changesPolygonColorBasedOnInvalidMessage() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                outputMode = OutputMode.GEOSHAPE,
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)
        mapFragment.click(MapPoint(0.0, 0.0))

        invalidMessage.value = "blah"
        val errorPolyLine = mapFragment.getPolygons()[0]
        assertThat(errorPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_ERROR_COLOR))
        assertThat(errorPolyLine.highlightLastPoint, equalTo(false))
        assertThat(
            errorPolyLine.getFillColor().opaque(),
            equalTo(MapConsts.DEFAULT_ERROR_COLOR.opaque())
        )

        invalidMessage.value = null
        val normalPolyLine = mapFragment.getPolygons()[0]
        assertThat(normalPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
        assertThat(normalPolyLine.highlightLastPoint, equalTo(true))
        assertThat(
            normalPolyLine.getFillColor().opaque(),
            equalTo(MapConsts.DEFAULT_STROKE_COLOR.opaque())
        )
    }

    @Test
    fun disablesSaveButtonWhenInvalid() {
        val invalidMessage = MutableLiveData<String?>(null)
        fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        invalidMessage.value = "Blah"
        Assertions.assertDisabled(withContentDescription(string.save))

        invalidMessage.value = null
        Assertions.assertEnabled(withContentDescription(string.save))
    }

    @Test
    fun whenOutputModeIsGeoTrace_setsChangeResultWheneverAPointIsAddedAfterTheFirst() {
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)

        mapFragment.click(MapPoint(0.0, 0.0))
        var result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        mapFragment.click(MapPoint(1.0, 1.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun whenOutputModeIsGeoShape_doesNotSetChangeResultUntilThereAre3Points() {
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)

        mapFragment.click(MapPoint(0.0, 0.0))
        var result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        mapFragment.click(MapPoint(1.0, 0.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        mapFragment.click(MapPoint(1.0, 1.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0;0.0 0.0 0.0 0.0")
        )
    }

    @Test
    fun setsChangeResultWheneverAPointIsRemoved() {
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                inputPolygon =
                    listOf(
                        MapPoint(0.0, 0.0),
                        MapPoint(1.0, 0.0),
                        MapPoint(1.0, 1.0)
                    )
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        Interactions.clickOn(withContentDescription(string.remove_last_point))
        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0")
        )
    }

    @Test
    fun setsChangeResultWheneverAPointIsMoved() {
        val inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 0.0), MapPoint(1.0, 1.0))
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        val lineId = mapFragment.getFeatureId(inputPolygon)
        mapFragment.dragPolyLine(lineId, inputPolygon.dropLast(1) + MapPoint(2.0, 2.0))

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0;2.0 2.0 0.0 0.0")
        )
    }

    @Test
    fun clickingClear_clearsPoints() {
        val inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 0.0), MapPoint(1.0, 1.0))
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        Interactions.clickOn(withContentDescription(string.clear))
        Interactions.clickOn(withText(string.clear), root = isDialog())
        assertThat(mapFragment.getPolyLines().first().points.size, equalTo(0))
    }

    @Test
    fun clickingClear_setsChangeResult() {
        val inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 0.0), MapPoint(1.0, 1.0))
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        Interactions.clickOn(withContentDescription(string.clear))
        Interactions.clickOn(withText(string.clear), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )
    }

    @Test
    fun recordingPointsAutomatically_setsChangeResult() {
        val scenario = fragmentLauncherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }
        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.automatic_mode)
        locationTracker.currentLocation = Location(1.0, 1.0, 1.0, 1f)
        scheduler.runForeground(0)
        locationTracker.currentLocation = Location(2.0, 2.0, 1.0, 1f)
        scheduler.runForeground(DEFAULT_RECORDING_INTERVAL)

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("1.0 1.0 1.0 1.0;2.0 2.0 1.0 1.0")
        )
    }

    companion object {
        private val DEFAULT_RECORDING_INTERVAL =
            INTERVAL_OPTIONS[GeoPolyFragment.DEFAULT_INTERVAL_INDEX].toLong() * 1000

        private fun startInput(mode: Int? = null) {
            onView(withId(R.id.play)).perform(click())

            if (mode != null) {
                onView(withId(mode)).inRoot(isDialog()).perform(click())
                onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            }
        }
    }
}
