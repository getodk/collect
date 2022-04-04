package org.odk.collect.geo.selection

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.R
import org.odk.collect.geo.ReferenceLayerSettingsNavigator
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.Fixtures
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass

@RunWith(AndroidJUnit4::class)
class SelectionMapFragmentTest {

    private val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()

    private val map = FakeMapFragment()
    private val referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator = mock()
    private val data = mock<SelectionMapData> {
        on { isLoading() } doReturn MutableNonNullLiveData(false)
        on { getMapTitle() } doReturn MutableLiveData("")
        on { getItemType() } doReturn "Things"
        on { getItemCount() } doReturn MutableLiveData(0)
        on { getMappableItems() } doReturn MutableNonNullLiveData(emptyList())
    }

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        R.style.Theme_MaterialComponents,
        object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return SelectionMapFragment(data)
            }
        }
    )

    @Before
    fun setup() {
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
            }).build()
    }

    @Test
    fun `updates markers when items update`() {
        val items: List<MappableSelectItem> = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        val itemsLiveData = MutableNonNullLiveData(items)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.getMarkers(), equalTo(itemsLiveData.value.map { it.toMapPoint() }))

        itemsLiveData.value = emptyList()
        assertThat(map.getMarkers(), equalTo(emptyList()))
    }

    @Test
    fun `updates item count when items update`() {
        val items: List<MappableSelectItem> = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0),
            Fixtures.actionMappableSelectItem().copy(id = 1)
        )

        val itemsLiveData = MutableNonNullLiveData(items)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        onView(withText(application.getString(R.string.select_item_count, "Things", 0, 2)))
            .check(matches(isDisplayed()))

        itemsLiveData.value = emptyList()
        onView(withText(application.getString(R.string.select_item_count, "Things", 0, 0)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `zooms to fit all items`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )

        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        assertThat(map.center, equalTo(FakeMapFragment.DEFAULT_CENTER))
        val points = items.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `zooms to current location when there are no items`() {
        whenever(data.getMappableItems()).doReturn(MutableNonNullLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.center, equalTo(FakeMapFragment.DEFAULT_CENTER))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
    }

    @Test
    fun `does not zoom to current location when it changes`() {
        whenever(data.getMappableItems()).doReturn(MutableNonNullLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        assertThat(map.center, equalTo(FakeMapFragment.DEFAULT_CENTER))

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
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        onView(withId(R.id.zoom_to_bounds)).perform(click())

        assertThat(map.center, equalTo(FakeMapFragment.DEFAULT_CENTER))
        val points = items.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
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
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.zoomToPoint(MapPoint(55.0, 66.0), 2.0, false)

        map.clickOnFeature(1)
        assertThat(map.center, equalTo(items[1].toMapPoint()))
        assertThat(map.zoom, equalTo(2.0))
    }

    @Test
    fun `tapping on item switches item marker to large icon`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(
                id = 0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
            Fixtures.actionMappableSelectItem().copy(
                id = 1,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(1)
        assertThat(map.getMarkerIcons()[0], equalTo(items[0].smallIcon))
        assertThat(map.getMarkerIcons()[1], equalTo(items[1].largeIcon))
    }

    @Test
    fun `tapping on item when another has been tapped switches the first one back to its small icon`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(
                id = 0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
            Fixtures.actionMappableSelectItem().copy(
                id = 1,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm
            ),
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(0)
        map.clickOnFeature(1)
        assertThat(map.getMarkerIcons()[0], equalTo(items[0].smallIcon))
        assertThat(map.getMarkerIcons()[1], equalTo(items[1].largeIcon))
    }

    @Test
    fun `tapping on item sets item on summary sheet`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, name = "Blah1"),
            Fixtures.actionMappableSelectItem().copy(id = 1, name = "Blah2"),
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)

        map.clickOnFeature(0)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.summary_sheet)),
                withText("Blah1")
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun `tapping on item returns item ID as result when skipSummary is true`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0),
            Fixtures.actionMappableSelectItem().copy(id = 1),
        )
        whenever(data.getMappableItems()).thenReturn(MutableNonNullLiveData(items))

        val scenario = launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(SelectionMapFragment::class.java) {
                    SelectionMapFragment(data, skipSummary = true)
                }.build()
        )

        var actualResult: Bundle? = null
        scenario.onFragment {
            it.parentFragmentManager.setFragmentResultListener(
                SelectionMapFragment.REQUEST_SELECT_ITEM,
                it
            ) { _: String?, result: Bundle ->
                actualResult = result
            }
        }

        map.clickOnFeature(0)
        assertThat(
            actualResult!!.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM),
            equalTo(items[0].id)
        )
    }

    @Test
    fun `hides new item button when showNewItemButton is false`() {
        launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(SelectionMapFragment::class.java) {
                    SelectionMapFragment(data, showNewItemButton = false)
                }.build()
        )

        onView(withContentDescription(R.string.new_item)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `ignores feature clicks for IDs that are not item features`() {
        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.clickOnFeature(-1)
        map.clickOnFeature(-2) // First click is fine but second could use the ID and crash
    }

    @Test
    fun `recreating maintains zoom`() {
        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.zoomToPoint(MapPoint(55.0, 66.0), 7.0, false)

        scenario.recreate()

        assertThat(map.getZoomBoundingBox(), equalTo(null))
        assertThat(map.center, equalTo(MapPoint(55.0, 66.0)))
        assertThat(map.zoom, equalTo(7.0))
    }

    @Test
    fun `shows progress dialog when loading`() {
        val loadingLiveData = MutableNonNullLiveData(false)
        whenever(data.isLoading()).thenReturn(loadingLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java).onFragment {
            val dialogClass = MaterialProgressDialogFragment::class.java
            assertThat(getFragmentByClass(it.childFragmentManager, dialogClass), nullValue())

            loadingLiveData.value = true
            assertThat(getFragmentByClass(it.childFragmentManager, dialogClass), notNullValue())

            loadingLiveData.value = false
            assertThat(getFragmentByClass(it.childFragmentManager, dialogClass), nullValue())
        }
    }

    private fun MappableSelectItem.toMapPoint(): MapPoint {
        return MapPoint(this.latitude, this.longitude)
    }
}
