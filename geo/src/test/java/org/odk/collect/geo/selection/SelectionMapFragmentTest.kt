package org.odk.collect.geo.selection

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.odk.collect.material.BottomSheetBehavior
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass

@RunWith(AndroidJUnit4::class)
class SelectionMapFragmentTest {

    private val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()

    private lateinit var map: FakeMapFragment
    private val referenceLayerSettingsNavigator: ReferenceLayerSettingsNavigator = mock()
    private val data = mock<SelectionMapData> {
        on { isLoading() } doReturn MutableNonNullLiveData(false)
        on { getMapTitle() } doReturn MutableLiveData("")
        on { getItemType() } doReturn "Things"
        on { getItemCount() } doReturn MutableNonNullLiveData(0)
        on { getMappableItems() } doReturn MutableLiveData(emptyList())
    }

    private val onBackPressedDispatcher = OnBackPressedDispatcher()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(
        R.style.Theme_MaterialComponents,
        object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return SelectionMapFragment(data, onBackPressedDispatcher = { onBackPressedDispatcher })
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
                        override fun createMapFragment(): MapFragment {
                            return FakeMapFragment().also {
                                map = it
                            }
                        }
                    }
                }

                override fun providesPermissionChecker(context: Context): PermissionsChecker {
                    return object : PermissionsChecker {
                        override fun isPermissionGranted(vararg permissions: String): Boolean {
                            return true
                        }
                    }
                }

                override fun providesReferenceLayerSettingsNavigator() =
                    referenceLayerSettingsNavigator
            }).build()

        BottomSheetBehavior.DRAGGING_ENABLED = false
    }

    @Test
    fun `summary sheet is hidden initially`() {
        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        onView(withId(R.id.summary_sheet)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `does not have enabled back callbacks`() {
        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        scenario.onFragment {
            assertThat(onBackPressedDispatcher.hasEnabledCallbacks(), equalTo(false))
        }
    }

    @Test
    fun `updates markers when items update`() {
        val items: List<MappableSelectItem> = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        val itemsLiveData = MutableLiveData(items)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        assertThat(map.getMarkers(), equalTo(itemsLiveData.value?.map { it.toMapPoint() }))

        itemsLiveData.value = emptyList()
        assertThat(map.getMarkers(), equalTo(emptyList()))
    }

    @Test
    fun `updates item count when items update`() {
        val items: List<MappableSelectItem> = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0),
            Fixtures.actionMappableSelectItem().copy(id = 1)
        )

        val itemsLiveData = MutableLiveData(items)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

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

        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        val points = items.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `does not zoom to fit all items again when they change`() {
        val originalItems =
            listOf(Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0))
        val itemsLiveData: MutableLiveData<List<MappableSelectItem>?> =
            MutableLiveData(originalItems)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        itemsLiveData.value =
            listOf(Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 52.0))

        val points = originalItems.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `does not zoom to fit all items if map already has center`() {
        val items = listOf(Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0))
        val itemsLiveData: MutableLiveData<List<MappableSelectItem>?> =
            MutableLiveData(items)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.setCenter(MapPoint(12.3, 45.6), false)
        map.ready()

        assertThat(map.center, equalTo(MapPoint(12.3, 45.6)))
    }

    @Test
    fun `zooms to current location when zoomToFitItems is false`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(SelectionMapFragment::class.java) {
                    SelectionMapFragment(
                        data,
                        zoomToFitItems = false,
                        onBackPressedDispatcher = { onBackPressedDispatcher }
                    )
                }.build()
        )
        map.ready()

        assertThat(map.hasCenter(), equalTo(false))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
    }

    @Test
    fun `zooms to current location when there are no items`() {
        whenever(data.getMappableItems()).doReturn(MutableLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        assertThat(map.hasCenter(), equalTo(false))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
        assertThat(map.zoom, equalTo(FakeMapFragment.DEFAULT_POINT_ZOOM))
    }

    @Test
    fun `does not zoom to current location when it changes`() {
        whenever(data.getMappableItems()).doReturn(MutableLiveData(emptyList()))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        assertThat(map.hasCenter(), equalTo(false))

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))

        map.setLocation(MapPoint(3.0, 4.0))
        assertThat(map.center, equalTo(MapPoint(1.0, 2.0)))
    }

    @Test
    fun `does not zoom to current location when items change`() {
        val originalItems =
            listOf(Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0))
        val itemsLiveData: MutableLiveData<List<MappableSelectItem>?> =
            MutableLiveData(originalItems)
        whenever(data.getMappableItems()).thenReturn(itemsLiveData)

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()
        map.setLocation(MapPoint(67.0, 48.0))

        itemsLiveData.value =
            listOf(Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 52.0))

        val points = originalItems.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `tapping current location button zooms to gps location`() {
        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.setLocation(MapPoint(40.181389, 44.514444))
        onView(withId(R.id.zoom_to_location)).perform(click())

        assertThat(map.center, equalTo(MapPoint(40.181389, 44.514444)))
        assertThat(map.zoom, equalTo(FakeMapFragment.DEFAULT_POINT_ZOOM))
    }

    @Test
    fun `tapping zoom to fit button zooms to fit all items`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0)
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        onView(withId(R.id.zoom_to_bounds)).perform(click())

        val points = items.map { it.toMapPoint() }
        assertThat(map.getZoomBoundingBox(), equalTo(Pair(points, 0.8)))
    }

    @Test
    fun `tapping layers button navigates to layers settings`() {
        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

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
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

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
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                symbol = "A",
                color = "#ffffff"
            ),
            Fixtures.actionMappableSelectItem().copy(
                id = 1,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                symbol = "B",
                color = "#000000"
            ),
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(1)

        val firstIcon = map.getMarkerIcons()[0]!!
        assertThat(firstIcon.icon, equalTo(items[0].smallIcon))
        assertThat(firstIcon.getSymbol(), equalTo("A"))
        assertThat(firstIcon.getColor(), equalTo(Color.parseColor("#ffffff")))

        val secondIcon = map.getMarkerIcons()[1]!!
        assertThat(secondIcon.icon, equalTo(items[1].largeIcon))
        assertThat(secondIcon.getSymbol(), equalTo("B"))
        assertThat(secondIcon.getColor(), equalTo(Color.parseColor("#000000")))
    }

    @Test
    fun `tapping on item when another has been tapped switches the first one back to its small icon`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(
                id = 0,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                symbol = "A",
                color = "#ffffff"
            ),
            Fixtures.actionMappableSelectItem().copy(
                id = 1,
                smallIcon = android.R.drawable.ic_lock_idle_charging,
                largeIcon = android.R.drawable.ic_lock_idle_alarm,
                symbol = "B",
                color = "#000000"
            ),
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        map.clickOnFeature(1)

        val firstIcon = map.getMarkerIcons()[0]!!
        assertThat(firstIcon.icon, equalTo(items[0].smallIcon))
        assertThat(firstIcon.getSymbol(), equalTo("A"))
        assertThat(firstIcon.getColor(), equalTo(Color.parseColor("#ffffff")))

        val secondIcon = map.getMarkerIcons()[1]!!
        assertThat(secondIcon.icon, equalTo(items[1].largeIcon))
        assertThat(secondIcon.getSymbol(), equalTo("B"))
        assertThat(secondIcon.getColor(), equalTo(Color.parseColor("#000000")))
    }

    @Test
    fun `tapping on item sets item on summary sheet`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, name = "Blah1"),
            Fixtures.actionMappableSelectItem().copy(id = 1, name = "Blah2"),
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Blah1")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `tapping on item returns item ID as result when skipSummary is true`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0),
            Fixtures.actionMappableSelectItem().copy(id = 1),
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        val scenario = launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(SelectionMapFragment::class.java) {
                    SelectionMapFragment(
                        data,
                        skipSummary = true,
                        onBackPressedDispatcher = { onBackPressedDispatcher }
                    )
                }.build()
        )
        map.ready()

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
    fun `clicking map with an item selected deselects it`() {
        val item = Fixtures.actionMappableSelectItem().copy(id = 0, name = "Blah1")
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(listOf(item)))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        map.click(MapPoint(0.0, 0.0))

        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Blah1")))
            .check(matches(not(isDisplayed())))
        assertThat(map.getMarkerIcons()[0]!!.icon, equalTo(item.smallIcon))
    }

    @Test
    fun `pressing back with an item selected deselects it`() {
        val item = Fixtures.actionMappableSelectItem()
            .copy(id = 0, name = "Blah1", symbol = "A", color = "#ffffff")
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(listOf(item)))

        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        scenario.onFragment {
            onBackPressedDispatcher.onBackPressed()
        }

        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Blah1")))
            .check(matches(not(isDisplayed())))

        assertThat(map.getMarkerIcons()[0]!!.icon, equalTo(item.smallIcon))
        assertThat(map.getMarkerIcons()[0]!!.getSymbol(), equalTo("A"))
        assertThat(map.getMarkerIcons()[0]!!.getColor(), equalTo(Color.parseColor("#ffffff")))
    }

    @Test
    fun `pressing back after deselecting item disables back callbacks`() {
        val item = Fixtures.actionMappableSelectItem().copy(id = 0, name = "Blah1")
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(listOf(item)))

        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        scenario.onFragment {
            onBackPressedDispatcher.onBackPressed()
            assertThat(onBackPressedDispatcher.hasEnabledCallbacks(), equalTo(false))
        }
    }

    @Test
    fun `recreating after deselecting item has no item selected`() {
        val items = listOf(Fixtures.actionMappableSelectItem().copy(id = 0, name = "Point1"))
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        scenario.onFragment {
            onBackPressedDispatcher.onBackPressed()
        }

        scenario.recreate()
        map.ready()

        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Point1")))
            .check(doesNotExist())
    }

    @Test
    fun `tapping action hides summary sheet`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(
                id = 0,
                name = "Item",
                action = MappableSelectItem.IconifiedText(null, "Action")
            )
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(0)
        onView(withText("Action")).perform(click())
        onView(withText("Item")).check(matches(not(isDisplayed())))
    }

    @Test
    fun `centers on already selected item`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0, selected = true)
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        assertThat(map.center, equalTo(items[1].toMapPoint()))
        assertThat(map.zoom, equalTo(FakeMapFragment.DEFAULT_POINT_ZOOM))
    }

    @Test
    fun `does not move when location changes when centered on already selected item`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0, selected = true)
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.setLocation(MapPoint(1.0, 2.0))
        assertThat(map.center, equalTo(items[1].toMapPoint()))
    }

    @Test
    fun `hides new item button when showNewItemButton is false`() {
        launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            factory = FragmentFactoryBuilder()
                .forClass(SelectionMapFragment::class.java) {
                    SelectionMapFragment(
                        data,
                        showNewItemButton = false,
                        onBackPressedDispatcher = { onBackPressedDispatcher }
                    )
                }.build()
        )
        map.ready()

        onView(withContentDescription(R.string.new_item)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `ignores feature clicks for IDs that are not item features`() {
        launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(-1)
        map.clickOnFeature(-2) // First click is fine but second could use the ID and crash
    }

    @Test
    fun `recreating maintains selection`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem().copy(id = 0, latitude = 40.0, name = "Point1"),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0, name = "Point2")
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(1)

        scenario.recreate()
        map.ready()

        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Point2")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `recreating with initial selection maintains new selection`() {
        val items = listOf(
            Fixtures.actionMappableSelectItem()
                .copy(id = 0, latitude = 40.0, name = "Point1", selected = true),
            Fixtures.actionMappableSelectItem().copy(id = 1, latitude = 41.0, name = "Point2")
        )
        whenever(data.getMappableItems()).thenReturn(MutableLiveData(items))

        val scenario = launcherRule.launchInContainer(SelectionMapFragment::class.java)
        map.ready()

        map.clickOnFeature(1)

        scenario.recreate()
        map.ready()

        onView(allOf(isDescendantOfA(withId(R.id.summary_sheet)), withText("Point2")))
            .check(matches(isDisplayed()))
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

    @Test
    fun `onDestroy works if the view was never created`() {
        val scenario = launcherRule.launchInContainer(
            SelectionMapFragment::class.java,
            initialState = Lifecycle.State.CREATED // `onCreateView` is called at `RESUMED`
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    private fun MappableSelectItem.toMapPoint(): MapPoint {
        return MapPoint(this.latitude, this.longitude)
    }
}
