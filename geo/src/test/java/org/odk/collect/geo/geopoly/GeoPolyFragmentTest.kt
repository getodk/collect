package org.odk.collect.geo.geopoly

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.utils.opaque
import org.odk.collect.androidtest.FragmentScenarioExtensions.setFragmentResultListener
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoint.AccuracyStatusView
import org.odk.collect.geo.geopoint.LocationAccuracy
import org.odk.collect.geo.geopoly.GeoPolyFragment.Companion.INTERVAL_OPTIONS
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.geo.items.MappableData
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.geo.support.AccuracyStatusViewMatcher.Companion.hasAccuracy
import org.odk.collect.geo.support.FakeLocationTracker
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.MapFragmentAssertions.hasZoomedToCurrentLocation
import org.odk.collect.geo.support.MapFragmentAssertions.showsCurrentLocation
import org.odk.collect.geo.support.MapFragmentAssertions.showsMappableData
import org.odk.collect.geo.support.MappableItemsFixtures
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.EspressoAssertions
import org.odk.collect.testshared.EspressoAssertions.assertNotVisible
import org.odk.collect.testshared.EspressoAssertions.assertVisible
import org.odk.collect.testshared.EspressoInteractions
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.FragmentResultRecorder
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.odk.collect.webpage.WebPageService
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPolyFragmentTest {
    private val map = FakeMapFragment(ready = true)

    private val locationTracker = FakeLocationTracker()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val scheduler = FakeScheduler()

    @Before
    fun setUp() {
        val shadowApplication =
            Shadows.shadowOf(application)
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION")
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION")
        overrideDependencies(map)

        SnackbarUtils.alertStore.enabled = true
    }

    @After
    fun teardown() {
        SnackbarUtils.alertStore.enabled = false
    }

    @Test
    fun zoomsToCurrentLocation() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(2.0, 2.0)
        assertThat(map, hasZoomedToCurrentLocation(MapPoint(2.0, 2.0)))
    }

    @Test
    fun marksCurrentLocation() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        val firstLocation = Location(2.0, 2.0, accuracy = 5.2f)
        locationTracker.currentLocation = firstLocation
        assertThat(map, showsCurrentLocation(firstLocation.toMapPoint()))

        val secondLocation = Location(3.0, 2.0, accuracy = 2.1f)
        locationTracker.currentLocation = secondLocation
        assertThat(map, showsCurrentLocation(secondLocation.toMapPoint()))
        assertThat(map, not(showsCurrentLocation(firstLocation.toMapPoint())))
    }

    @Test
    fun showsCurrentLocationAccuracy() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(2.0, 2.0, accuracy = 3.1f)
        val accuracy = LocationAccuracy.Improving(3.1f)
        onView(isAssignableFrom(AccuracyStatusView::class.java)).check(matches(hasAccuracy(accuracy)))
    }

    @Test
    fun recordButton_should_beHiddenForAutomaticMode() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun recordButton_should_beVisibleForManualMode() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.manual_mode)
        onView(withId(R.id.record_button)).check(matches(isDisplayed()))
    }

    @Test
    fun placingPoint_updatesCollectionStatus() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        assertVisible(withText(application.getString(string.collection_status_placement, 1)))
    }

    @Test
    fun recordingPointsAutomatically_updatesCollectionStatus() {
        launcherRule.launchInContainer {
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
    fun recordingPointsAutomatically_updatesAccuracyBasedOnThreshold() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        val unacceptable = LocationAccuracy.Unacceptable(11.0f)
        locationTracker.currentLocation = Location(1.0, 1.0, accuracy = unacceptable.value)
        startInput(R.id.automatic_mode)
        onView(isAssignableFrom(AccuracyStatusView::class.java)).check(
            matches(
                hasAccuracy(
                    unacceptable
                )
            )
        )

        val improving = LocationAccuracy.Improving(9.0f)
        locationTracker.currentLocation = Location(1.0, 1.0, accuracy = improving.value)
        onView(isAssignableFrom(AccuracyStatusView::class.java)).check(matches(hasAccuracy(improving)))
    }

    @Test
    fun recordingPointsAutomatically_doesNotRecordFasterThanInterval() {
        launcherRule.launchInContainer {
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
        launcherRule.launchInContainer {
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

        EspressoInteractions.clickOn(withContentDescription(string.pause_location_recording))
        locationTracker.currentLocation = Location(2.0, 2.0)
        scheduler.runForeground(DEFAULT_RECORDING_INTERVAL)
        assertVisible(
            withText(
                application.getString(string.collection_status_paused, 1)
            )
        )
    }

    @Test
    fun recordingPointsAutomatically_usesCurrentLocationWhenThereIsOne() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(1.0, 1.0)
        startInput(R.id.automatic_mode)
        scheduler.runForeground(0)
        assertVisible(
            withText(
                application.getString(string.collection_status_auto_seconds_accuracy, 1, 20, 10)
            )
        )
    }

    @Test
    fun whenPolygonExtraPresent_showsPoly() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = polygon)
        }

        val polys = map.getPolyLines()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points, equalTo(polygon))
    }

    @Test
    fun whenPolygonExtraPresent_andOutputModeIsShape_showsClosedPoly() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        polygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE,
                inputPolygon = polygon
            )
        }

        val polys = map.getPolygons()
        assertThat(polys.size, equalTo(1))
        val expectedPolygon = ArrayList<MapPoint>()
        expectedPolygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        expectedPolygon.add(MapPoint(2.0, 3.0, 3.0, 4.0))
        assertThat(polys[0].points, equalTo(expectedPolygon))
    }

    @Test
    fun whenPolygonExtraPresent_andPolyIsEmpty_andOutputModeIsShape_doesNotShowPoly() {
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE,
                inputPolygon = emptyList()
            )
        }

        val polys = map.getPolygons()
        assertThat(polys.size, equalTo(1))
        assertThat(polys[0].points.isEmpty(), equalTo(true))
    }

    @Test
    fun pressingBack_setsCancelledResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = launcherRule.launchInContainer {
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
        val scenario = launcherRule.launchInContainer {
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
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment({ onBackPressedDispatcher })
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        EspressoInteractions.clickOn(withText(string.cancel), root = isDialog())

        val result = resultListener.getAll().lastOrNull()
        assertThat(result, equalTo(null))
    }

    @Test
    fun whenPolygonHasBeenCreated_pressingBack_andClickingDiscard_setsEmptyResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment({ onBackPressedDispatcher })
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        map.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        EspressoInteractions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(result.second.getString(GeoPolyFragment.RESULT_GEOPOLY), equalTo(""))
    }

    @Test
    fun whenPolygonHasBeenModified_pressingBack_andClickingDiscard_setsOriginalAsResult() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0))
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        map.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        EspressoInteractions.clickOn(withText(string.discard), root = isDialog())

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
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0))
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        map.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        onBackPressedDispatcher.onBackPressed()
        EspressoInteractions.clickOn(withText(string.discard), root = isDialog())

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
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0))
            )
        }
        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput()
        map.click(MapPoint(2.0, 2.0))
        resultListener.clear()

        scenario.recreate()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        onBackPressedDispatcher.onBackPressed()
        EspressoInteractions.clickOn(withText(string.discard), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun recordingPointsAutomatically_usesRetainMockAccuracyTrueToStartLocationTracker() {
        launcherRule.launchInContainer {
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
        launcherRule.launchInContainer {
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
        launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.manual_mode)
        locationTracker.currentLocation = Location(1.0, 1.0)
        onView(withId(R.id.record_button)).perform(click())
        onView(withId(R.id.record_button)).perform(click())
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun placingPoint_whenPointIsADuplicateOfTheLastPoint_skipsPoint() {
        launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        map.click(MapPoint(1.0, 1.0))
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun buttonsShouldBeEnabledInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                false,
                polyline
            )
        }

        EspressoAssertions.assertEnabled(withContentDescription(string.input_method))
        EspressoAssertions.assertEnabled(withContentDescription(string.remove_last_point))
        EspressoAssertions.assertEnabled(withContentDescription(string.clear))
        EspressoAssertions.assertEnabled(withContentDescription(string.save))
    }

    @Test
    fun buttonsShouldBeDisabledInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                true,
                false,
                polygon
            )
        }

        EspressoAssertions.assertDisabled(withContentDescription(string.input_method))
        EspressoAssertions.assertDisabled(withContentDescription(string.remove_last_point))
        EspressoAssertions.assertDisabled(withContentDescription(string.clear))
        EspressoAssertions.assertDisabled(withContentDescription(string.save))
    }

    @Test
    fun polyShouldBeDraggableInEditableMode() {
        val polyline = ArrayList<MapPoint>()
        polyline.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                false,
                false,
                polyline
            )
        }

        assertThat(map.isPolyDraggable(0), equalTo(true))
    }

    @Test
    fun polyShouldNotBeDraggableInReadOnlyMode() {
        val polygon = ArrayList<MapPoint>()
        polygon.add(MapPoint(1.0, 2.0, 3.0, 4.0))
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE,
                true,
                false,
                polygon
            )
        }

        assertThat(map.isPolyDraggable(0), equalTo(false))
    }

    @Test
    fun recreatingTheFragmentWithTheLayersDialogDisplayedDoesNotCrashTheApp() {
        val scenario =
            launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        onView(withId(R.id.layers)).perform(click())

        scenario.recreate()
    }

    @Test
    fun whenInvalidMessageIsNotNull_pointsCanBeAddedByClicking() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)

        invalidMessage.value = DisplayString.Raw("Blah")
        map.click(MapPoint(0.0, 0.0))
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun whenInvalidMessageIsNotNull_pointsCannotBeAddedManually() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.manual_mode)

        invalidMessage.value = DisplayString.Raw("Blah")
        EspressoInteractions.clickOn(withContentDescription(string.record_geopoint))
        assertThat(map.getPolyLines()[0].points.size, equalTo(0))
    }

    @Test
    fun whenInvalidMessageIsNotNull_automaticRecordingDoesNotStop() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.automatic_mode)

        invalidMessage.value = DisplayString.Raw("Blah")
        locationTracker.currentLocation = Location(1.0, 1.0, 1.0, 1f)
        scheduler.runForeground(0)
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun showsAndHidesInvalidMessageSnackbarBasedOnValue() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        val message = "Something is wrong"
        invalidMessage.value = DisplayString.Raw(message)
        assertVisible(withText(message))

        invalidMessage.value = null
        assertNotVisible(withText(message))
        EspressoAssertions.assertAlert(
            SnackbarUtils.alertStore,
            application.getString(string.error_fixed),
            "No error fixed message shown!"
        )
    }

    @Test
    fun invalidSnackbarCanBeDismissed() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        val message = "Something is wrong"
        invalidMessage.value = DisplayString.Raw(message)
        EspressoInteractions.clickOn(withContentDescription(string.close_snackbar))
        assertNotVisible(withText(message))
    }

    @Test
    fun changesPolyLineColorBasedOnInvalidMessage() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(0.0, 0.0))

        invalidMessage.value = DisplayString.Raw("blah")
        val errorPolyLine = map.getPolyLines()[0]
        assertThat(errorPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_ERROR_COLOR))
        assertThat(errorPolyLine.highlightLastPoint, equalTo(false))

        invalidMessage.value = null
        val normalPolyLine = map.getPolyLines()[0]
        assertThat(normalPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
        assertThat(normalPolyLine.highlightLastPoint, equalTo(true))
    }

    @Test
    fun changesPolygonColorBasedOnInvalidMessage() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                outputMode = OutputMode.GEOSHAPE,
                invalidMessage = invalidMessage
            )
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(0.0, 0.0))

        invalidMessage.value = DisplayString.Raw("blah")
        val errorPolyLine = map.getPolygons()[0]
        assertThat(errorPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_ERROR_COLOR))
        assertThat(errorPolyLine.highlightLastPoint, equalTo(false))
        assertThat(
            errorPolyLine.getFillColor().opaque(),
            equalTo(MapConsts.DEFAULT_ERROR_COLOR.opaque())
        )

        invalidMessage.value = null
        val normalPolyLine = map.getPolygons()[0]
        assertThat(normalPolyLine.getStrokeColor(), equalTo(MapConsts.DEFAULT_STROKE_COLOR))
        assertThat(normalPolyLine.highlightLastPoint, equalTo(true))
        assertThat(
            normalPolyLine.getFillColor().opaque(),
            equalTo(MapConsts.DEFAULT_STROKE_COLOR.opaque())
        )
    }

    @Test
    fun disablesSaveButtonWhenInvalid() {
        val invalidMessage = MutableLiveData<DisplayString?>(null)
        launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                invalidMessage = invalidMessage
            )
        }

        invalidMessage.value = DisplayString.Raw("Blah")
        EspressoAssertions.assertDisabled(withContentDescription(string.save))

        invalidMessage.value = null
        EspressoAssertions.assertEnabled(withContentDescription(string.save))
    }

    @Test
    fun whenOutputModeIsGeoTrace_setsChangeResultWheneverAPointIsAddedAfterTheFirst() {
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOTRACE
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)

        map.click(MapPoint(0.0, 0.0))
        var result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        map.click(MapPoint(1.0, 1.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0")
        )
    }

    @Test
    fun whenOutputModeIsGeoShape_doesNotSetChangeResultUntilThereAre3Points() {
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { OnBackPressedDispatcher() },
                OutputMode.GEOSHAPE
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        startInput(R.id.placement_mode)

        map.click(MapPoint(0.0, 0.0))
        var result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        map.click(MapPoint(1.0, 0.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )

        map.click(MapPoint(1.0, 1.0))
        result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("0.0 0.0 0.0 0.0;1.0 0.0 0.0 0.0;1.0 1.0 0.0 0.0;0.0 0.0 0.0 0.0")
        )
    }

    @Test
    fun setsChangeResultWheneverAPointIsRemoved() {
        val scenario = launcherRule.launchInContainer {
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

        EspressoInteractions.clickOn(withContentDescription(string.remove_last_point))
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
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        val lineId = map.getFeatureId(inputPolygon)
        map.dragPolyLine(lineId, inputPolygon.dropLast(1) + MapPoint(2.0, 2.0))

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
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        EspressoInteractions.clickOn(withContentDescription(string.clear))
        EspressoInteractions.clickOn(withText(string.clear), root = isDialog())
        assertThat(map.getPolyLines().first().points.size, equalTo(0))
    }

    @Test
    fun clickingClear_setsChangeResult() {
        val inputPolygon = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 0.0), MapPoint(1.0, 1.0))
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, inputPolygon = inputPolygon)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        EspressoInteractions.clickOn(withContentDescription(string.clear))
        EspressoInteractions.clickOn(withText(string.clear), root = isDialog())

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(
            result.second.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE),
            equalTo("")
        )
    }

    @Test
    fun recordingPointsAutomatically_setsChangeResult() {
        val scenario = launcherRule.launchInContainer {
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

    @Test
    fun clickingZoom_zoomsToCurrentLocation() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(5.0, 5.0)
        locationTracker.currentLocation = Location(6.0, 6.0)

        EspressoInteractions.clickOn(withContentDescription(string.show_my_location))
        assertThat(map, hasZoomedToCurrentLocation(MapPoint(6.0, 6.0)))
    }

    @Test
    fun whenAutomaticallyRecordingLocation_mapCenterUpdates() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        locationTracker.currentLocation = Location(5.0, 5.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))

        locationTracker.currentLocation = Location(1.0, 1.0)
        assertThat(map.getCenter(), equalTo(MapPoint(1.0, 1.0)))
    }

    @Test
    fun whenNotRecordingLocation_mapCenterDoesNoUpdate() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(5.0, 5.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))

        locationTracker.currentLocation = Location(1.0, 1.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))
    }

    @Test
    fun whenRecordingIsPaused_mapCenterDoesNoUpdate() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        locationTracker.currentLocation = Location(5.0, 5.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))

        EspressoInteractions.clickOn(withContentDescription(string.pause_location_recording))
        locationTracker.currentLocation = Location(1.0, 1.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))
    }

    @Test
    fun showsItemsFromMappableData() {
        val mappableData = FakeMappableData(
            listOf(
                MappableItemsFixtures.point(),
                MappableItemsFixtures.actionMappableLine(),
                MappableItemsFixtures.actionMappablePolygon()
            )
        )

        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, mappableData = mappableData)
        }

        assertThat(map, showsMappableData(mappableData, background = true, clickable = false))
    }

    @Test
    fun showsProgressWhileLoadingItems() {
        val mappableData = FakeMappableData(emptyList())
        mappableData.isLoading = false

        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, mappableData = mappableData)
        }.onFragment {
            val dialogClass = MaterialProgressDialogFragment::class.java
            MatcherAssert.assertThat(
                getFragmentByClass(it.childFragmentManager, dialogClass),
                nullValue()
            )

            mappableData.isLoading = true
            assertThat(
                getFragmentByClass(it.childFragmentManager, dialogClass),
                notNullValue()
            )

            mappableData.isLoading = false
            assertThat(
                getFragmentByClass(it.childFragmentManager, dialogClass),
                nullValue()
            )
        }
    }

    private fun overrideDependencies(mapFragment: MapFragment) {
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
    }

    private fun startInput(mode: Int? = null) {
        onView(withId(R.id.play)).perform(click())

        if (mode != null) {
            onView(withId(mode)).inRoot(isDialog()).perform(click())
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
        }
    }
}

private val DEFAULT_RECORDING_INTERVAL =
    INTERVAL_OPTIONS[GeoPolyFragment.DEFAULT_INTERVAL_INDEX].toLong() * 1000

private class FakeMappableData(private val items: List<MappableItem>) : MappableData {

    var isLoading = false
        set(value) {
            _isLoading.value = value
            field = value
        }

    private val _isLoading = MutableNonNullLiveData(isLoading)

    override fun getMappableItems(): LiveData<List<MappableItem>?> {
        return MutableLiveData(items)
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return _isLoading
    }
}
