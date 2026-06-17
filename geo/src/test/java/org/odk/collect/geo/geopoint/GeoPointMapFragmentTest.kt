package org.odk.collect.geo.geopoint

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.androidtest.FragmentScenarioExtensions.setFragmentResultListener
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.R
import org.odk.collect.geo.geopoly.GeoPolyFragment
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
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.circles.CurrentLocationDelegate
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.EspressoAssertions
import org.odk.collect.testshared.EspressoInteractions
import org.odk.collect.testshared.FragmentResultRecorder
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.odk.collect.webpage.WebPageService
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPointMapFragmentTest {
    private val map = FakeMapFragment(ready = true)

    private val locationTracker = FakeLocationTracker()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        val shadowApplication =
            Shadows.shadowOf(application)
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION")
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION")
        overrideDependencies(map)
    }

    @Test
    fun `displays please wait message when location not set`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        EspressoAssertions.assertVisible(withText(string.please_wait_long))
    }

    @Test
    fun `displays status message when location set`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        locationTracker.currentLocation = Location(1.0, 2.0, 3.0, 4.0f)
        EspressoAssertions.assertVisible(withText("Accuracy: 4 m"))
    }

    @Test
    fun `returns point from first location fix`() {
        val scenario = launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        val resultListener = FragmentResultRecorder()
        scenario.setFragmentResultListener(GeoPointMapFragment.REQUEST_GEOPOINT, resultListener)

        val firstLocation = Location(1.0, 2.0, 3.0, 4.0f)
        locationTracker.currentLocation = firstLocation
        locationTracker.currentLocation = Location(5.0, 6.0, 7.0, 8.0f)

        EspressoInteractions.clickOn(withContentDescription(string.save))
        val result = resultListener.getAll().last()
        assertThat(result.first, equalTo(GeoPointMapFragment.REQUEST_GEOPOINT))
        assertThat(
            result.second.getString(GeoPointMapFragment.RESULT_GEOPOINT),
            equalTo("1.0 2.0 3.0 4.0")
        )
    }

    @Test
    fun `shows marker at first location fix`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        val firstLocation = Location(1.0, 2.0, 3.0, 4.0f)
        locationTracker.currentLocation = firstLocation
        locationTracker.currentLocation = Location(5.0, 6.0, 7.0, 8.0f)

        val markers = map.getMarkers()
            .filter { it.iconDescription != CurrentLocationDelegate.ICON_DESCRIPTION }
        assertThat(markers.size, equalTo(1))
        assertThat(markers[0].point, equalTo(firstLocation.toMapPoint()))
    }

    @Test
    fun `clicking add marker moves marker to the current location`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        locationTracker.currentLocation = Location(1.0, 2.0, 3.0, 4.0f)
        val secondLocation = Location(5.0, 6.0, 7.0, 8.0f)
        locationTracker.currentLocation = secondLocation

        EspressoInteractions.clickOn(withContentDescription(string.record_geopoint))

        val markers = map.getMarkers()
            .filter { it.iconDescription != CurrentLocationDelegate.ICON_DESCRIPTION }
        assertThat(markers.size, equalTo(1))
        assertThat(markers[0].point, equalTo(secondLocation.toMapPoint()))
    }

    @Test
    fun `shows marker when input point provided`() {
        val inputPoint = MapPoint(1.0, 2.0)
        launcherRule.launchInContainer {
            GeoPointMapFragment(inputPoint, false, false, false)
        }

        val markers = map.getMarkers()
        assertThat(markers.size, equalTo(1))
        assertThat(markers[0].point.latitude, equalTo(1.0))
        assertThat(markers[0].point.longitude, equalTo(2.0))
    }

    @Test
    fun `passing retain mock accuracy extra updates location tracker`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, true)
        }

        assertThat(locationTracker.retainMockAccuracy, equalTo(true))

        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        assertThat(locationTracker.retainMockAccuracy, equalTo(false))
    }

    @Test
    fun `recreating the fragment with the layers dialog displayed does not crash the app`() {
        val scenario =
            launcherRule.launchInContainer { GeoPointMapFragment(null, false, false, false) }

        onView(withId(R.id.layer_menu)).perform(click())

        scenario.recreate()
    }

    @Test
    fun `clicking zoom zooms to the current location`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        locationTracker.currentLocation = Location(5.0, 5.0)
        locationTracker.currentLocation = Location(6.0, 6.0)

        EspressoInteractions.clickOn(withContentDescription(string.show_my_location))
        assertThat(map, hasZoomedToCurrentLocation(MapPoint(6.0, 6.0)))
    }

    @Test
    fun `shows current location`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
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
    fun `clicking clear clears marker`() {
        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false)
        }

        val location = Location(2.0, 2.0, accuracy = 5.2f)
        locationTracker.currentLocation = location

        EspressoInteractions.clickOn(withContentDescription(string.clear))
        assertThat(map.getMarkers().size, equalTo(1))
        assertThat(map, showsCurrentLocation(location.toMapPoint()))
    }

    @Test
    fun `enables place marker button when existing location cleared`() {
        val inputPoint = MapPoint(1.0, 2.0)
        launcherRule.launchInContainer {
            GeoPointMapFragment(inputPoint, false, false, false)
        }

        EspressoAssertions.assertDisabled(withContentDescription(string.record_geopoint))
        EspressoInteractions.clickOn(withContentDescription(string.clear))
        EspressoAssertions.assertEnabled(withContentDescription(string.record_geopoint))
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
            GeoPointMapFragment(null, false, false, false, mappableData)
        }

        assertThat(map, showsMappableData(mappableData, background = true, clickable = false))
    }

    @Test
    fun `shows progress while loading mappable items`() {
        val mappableData = FakeMappableData(emptyList())
        mappableData.isLoading = false

        launcherRule.launchInContainer {
            GeoPointMapFragment(null, false, false, false, mappableData)
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
}
