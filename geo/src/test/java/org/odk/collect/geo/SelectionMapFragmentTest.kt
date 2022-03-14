package org.odk.collect.geo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.geo.maps.MapPoint
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.Fixtures
import org.odk.collect.permissions.PermissionsChecker

@RunWith(AndroidJUnit4::class)
class SelectionMapFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(R.style.Theme_MaterialComponents)

    private val map = FakeMapFragment()
    private val referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator = mock()
    private val viewModel = mock<SelectionMapViewModel> {
        on { getMapTitle() } doReturn MutableLiveData("")
        on { getItemCount() } doReturn MutableLiveData(0)
        on { getMappableItems() } doReturn MutableLiveData(emptyList())
    }

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()

        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesMapFragmentFactory(): MapFragmentFactory {
                    return object : MapFragmentFactory {
                        override fun createMapFragment(context: Context): MapFragment {
                            return map
                        }
                    }
                }

                override fun providesPermissionChecker(context: Context): PermissionsChecker {
                    return object : PermissionsChecker(context) {
                        override fun isPermissionGranted(vararg permissions: String): Boolean {
                            return true
                        }
                    }
                }

                override fun providesReferenceLayerSettingsNavigator() =
                    referenceLayerSettingsNavigator

                override fun providesSelectionMapViewMOdelFactory(): SelectionMapViewModelFactory {
                    return object : SelectionMapViewModelFactory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                            return viewModel as T
                        }
                    }
                }
            }).build()
    }

    @Test
    fun `updates markers when items update`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            )
        )
        val itemsLiveData = MutableLiveData(items)
        whenever(viewModel.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.markers, equalTo(itemsLiveData.value!!.map { it.toMapPoint() }))

        itemsLiveData.value = emptyList()
        assertThat(map.markers, equalTo(emptyList()))
    }

    @Test
    fun `zooms to fit all items`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            )
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        assertThat(map.center, equalTo(null))
        val points = items.map { it.toMapPoint() }
        assertThat(map.zoomBoundingBox, equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `zooms to current location when there are no items`() {
        whenever(viewModel.getMappableItems()).doReturn(MutableLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.center, equalTo(null))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
    }

    @Test
    fun `does not zoom to current location when it changes`() {
        whenever(viewModel.getMappableItems()).doReturn(MutableLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.center, equalTo(null))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))

        map.setLocation(MapPoint(3.0, 4.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
    }

    @Test
    fun `tapping current location button zooms to gps location`() {
        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.setLocation(MapPoint(40.181389, 44.514444))
        onView(withId(R.id.zoom_to_location)).perform(click())

        assertThat(map.center, equalTo(MapPoint(40.181389, 44.514444)))
    }

    @Test
    fun `tapping zoom to fit button zooms to fit all items`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            )
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        onView(withId(R.id.zoom_to_bounds)).perform(click())

        assertThat(map.center, equalTo(null))
        val points = items.map { it.toMapPoint() }
        assertThat(map.zoomBoundingBox, equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `tapping layers button navigates to layers settings`() {
        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)

        onView(withId(R.id.layer_menu)).perform(click())

        scenario.onFragment {
            verify(referenceLayerSettingsNavigator).navigateToReferenceLayerSettings(it.requireActivity())
        }
    }

    @Test
    fun `tapping on item centers on that item with current zoom level`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_power_off,
                largeIcon = android.R.drawable.ic_lock_idle_charging
            )
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.zoomToPoint(MapPoint(55.0, 66.0), 2.0, false)

        map.clickOnFeature(1)
        assertThat(map.center, equalTo(items[1].toMapPoint()))
        assertThat(map.zoom, equalTo(2.0))
    }

    @Test
    fun `tapping on item switches item marker to large icon`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(1)
        assertThat(map.markerIcons[0], equalTo(items[0].smallIcon))
        assertThat(map.markerIcons[1], equalTo(items[1].largeIcon))
    }

    @Test
    fun `tapping on item when another has been tapped switches the first one back to its small icon`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(0)
        map.clickOnFeature(1)
        assertThat(map.markerIcons[0], equalTo(items[0].smallIcon))
        assertThat(map.markerIcons[1], equalTo(items[1].largeIcon))
    }

    @Test
    fun `tapping on item sets item on summary sheet`() {
        val items = listOf(
            Fixtures.mappableSelectItem().copy(
                id = 0,
                latitude = 40.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                name = "Blah1"
            ),
            Fixtures.mappableSelectItem().copy(
                id = 1,
                latitude = 41.0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                name = "Blah2"
            ),
        )
        whenever(viewModel.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(0)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.summary_sheet)),
                withText("Blah1")
            )
        ).check(matches(isDisplayed()))

        map.clickOnFeature(1)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.summary_sheet)),
                withText("Blah2")
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun `recreating maintains zoom`() {
        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.zoomToPoint(MapPoint(55.0, 66.0), 7.0, false)

        scenario.recreate()

        assertThat(map.zoomBoundingBox, equalTo(null))
        assertThat(map.center, equalTo(MapPoint(55.0, 66.0)))
        assertThat(map.zoom, equalTo(7.0))
    }

    private fun MappableSelectItem.toMapPoint(): MapPoint {
        return MapPoint(this.latitude, this.longitude)
    }
}
