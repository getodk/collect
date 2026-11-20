package org.odk.collect.geo.geopoly

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
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
import org.odk.collect.geo.support.AccuracyStatusViewMatcher.Companion.hasAccuracy
import org.odk.collect.geo.support.FakeLocationTracker
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.FakeMappableData
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
    fun `zooms to current location`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(2.0, 2.0)
        assertThat(map, hasZoomedToCurrentLocation(MapPoint(2.0, 2.0)))
    }

    @Test
    fun `marks current location`() {
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
    fun `shows current location accuracy`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(2.0, 2.0, accuracy = 3.1f)
        val accuracy = LocationAccuracy.Improving(3.1f)
        onView(isAssignableFrom(AccuracyStatusView::class.java)).check(matches(hasAccuracy(accuracy)))
    }

    @Test
    fun `record button is hidden in automatic mode`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.automatic_mode)
        onView(withId(R.id.record_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `record button is visible in manual mode`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.manual_mode)
        onView(withId(R.id.record_button)).check(matches(isDisplayed()))
    }

    @Test
    fun `placing point updates collection status`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        assertVisible(withText(application.getString(string.collection_status_placement, 1)))
    }

    @Test
    fun `recording points automatically updates collection status`() {
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
    fun `recording points automatically updates accuracy based on threshold`() {
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
    fun `recording points automatically does not record faster than interval`() {
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
    fun `recording points automatically stops when pause is clicked`() {
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
    fun `recording points automatically uses current location when available`() {
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
    fun `shows polygon when polygon extra is present`() {
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
    fun `shows closed polygon in shape mode when polygon extra is present`() {
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
    fun `does not show polygon in shape mode when polygon is empty`() {
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
    fun `pressing back sets cancelled result`() {
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
    fun `pressing back sets cancelled result when input polygon is not empty`() {
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
    fun `pressing back sets cancel result when polygon has not been modified`() {
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val scenario = launcherRule.launchInContainer {
            GeoPolyFragment(
                { onBackPressedDispatcher },
                OutputMode.GEOSHAPE,
                inputPolygon = listOf(
                    MapPoint(1.0, 2.0, 3.0, 4.0),
                    MapPoint(2.0, 3.0, 3.0, 4.0),
                    MapPoint(1.0, 2.0, 3.0, 4.0)
                )
            )
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPolyFragment.REQUEST_GEOPOLY, resultListener)

        onBackPressedDispatcher.onBackPressed()

        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPolyFragment.REQUEST_GEOPOLY))
        assertThat(result.second.getString(GeoPolyFragment.RESULT_GEOPOLY), equalTo(null))
    }

    @Test
    fun `pressing back and clicking cancel sets no result when polygon has been modified`() {
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
    fun `pressing back and clicking discard sets empty result when polygon has been created`() {
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
    fun `pressing back and clicking discard sets original result when polygon has been modified`() {
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
    fun `pressing back and clicking discard sets original result when input polygon is only one point and has been modified`() {
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
    fun `pressing back and clicking discard sets original result when polygon has been modified and fragment is recreated`() {
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
    fun `recording points automatically starts location tracker with retain mock accuracy true`() {
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
    fun `recording points automatically starts location tracker with retain mock accuracy false`() {
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
    fun `skips point when manually recording duplicate of last point`() {
        launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.manual_mode)
        locationTracker.currentLocation = Location(1.0, 1.0)
        onView(withId(R.id.record_button)).perform(click())
        onView(withId(R.id.record_button)).perform(click())
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun `skips point when placing duplicate of last point`() {
        launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        map.click(MapPoint(1.0, 1.0))
        assertThat(map.getPolyLines()[0].points.size, equalTo(1))
    }

    @Test
    fun `buttons are enabled in editable mode`() {
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
    fun `buttons are disabled in read-only mode`() {
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
    fun `polygon is draggable in editable mode`() {
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
    fun `polygon is not draggable in read-only mode`() {
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
    fun `does not crash when fragment is recreated with layers dialog displayed`() {
        val scenario =
            launcherRule.launchInContainer { GeoPolyFragment({ OnBackPressedDispatcher() }) }

        onView(withId(R.id.layers)).perform(click())

        scenario.recreate()
    }

    @Test
    fun `points can be added by clicking when invalid message is not null`() {
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
    fun `points cannot be added manually when invalid message is not null`() {
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
    fun `automatic recording does not stop when invalid message is not null`() {
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
    fun `shows and hides invalid message snackbar based on value`() {
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
    fun `invalid snackbar can be dismissed`() {
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
    fun `polyline color changes based on invalid message`() {
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
    fun `polygon color changes based on invalid message`() {
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
    fun `save button is disabled when invalid`() {
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
    fun `sets change result when point is added in GeoTrace mode after first point`() {
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
    fun `does not set change result in GeoShape mode until there are 3 points`() {
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
    fun `sets change result when point is removed`() {
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
    fun `sets change result when point is moved`() {
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
    fun `clears points when clear is clicked`() {
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
    fun `sets change result when clear is clicked`() {
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
    fun `sets change result when recording points automatically`() {
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
    fun `zooms to current location when zoom is clicked`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(5.0, 5.0)
        locationTracker.currentLocation = Location(6.0, 6.0)

        EspressoInteractions.clickOn(withContentDescription(string.show_my_location))
        assertThat(map, hasZoomedToCurrentLocation(MapPoint(6.0, 6.0)))
    }

    @Test
    fun `map center updates when automatically recording location`() {
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
    fun `map center does not update when not recording location`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        locationTracker.currentLocation = Location(5.0, 5.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))

        locationTracker.currentLocation = Location(1.0, 1.0)
        assertThat(map.getCenter(), equalTo(MapPoint(5.0, 5.0)))
    }

    @Test
    fun `map center does not update when recording is paused`() {
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
    fun `shows items from mappable data`() {
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
    fun `shows progress while loading items`() {
        val mappableData = FakeMappableData(emptyList())
        mappableData.isLoading = false

        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() }, mappableData = mappableData)
        }.onFragment {
            val dialogClass = MaterialProgressDialogFragment::class.java
            assertThat(
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

    @Test
    fun `placed polylines are not clickable`() {
        launcherRule.launchInContainer {
            GeoPolyFragment({ OnBackPressedDispatcher() })
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        assertThat(map.getPolyLines()[0].clickable, equalTo(false))
    }

    @Test
    fun `placed polygons are not clickable`() {
        launcherRule.launchInContainer {
            GeoPolyFragment(
                outputMode = OutputMode.GEOSHAPE,
                onBackPressedDispatcher = { OnBackPressedDispatcher() })
        }

        startInput(R.id.placement_mode)
        map.click(MapPoint(1.0, 1.0))
        assertThat(map.getPolygons()[0].clickable, equalTo(false))
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
