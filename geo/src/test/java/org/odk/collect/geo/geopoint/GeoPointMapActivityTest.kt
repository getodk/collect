package org.odk.collect.geo.geopoint

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.async.Scheduler
import org.odk.collect.externalapp.ExternalAppUtils.getReturnedSingleValue
import org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY
import org.odk.collect.geo.DaggerGeoDependencyComponent
import org.odk.collect.geo.GeoDependencyModule
import org.odk.collect.geo.support.FakeLocationTracker
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.geo.support.RobolectricApplication
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R
import org.odk.collect.webpage.WebPageService
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class GeoPointMapActivityTest {

    private val mapFragment = FakeMapFragment()

    private val locationTracker = FakeLocationTracker()

    @get:Rule
    val launcherRule: ActivityScenarioLauncherRule = ActivityScenarioLauncherRule()

    @Before
    fun setUp() {
        val shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application?>())
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION")
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION")

        val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
        application.geoDependencyComponent = DaggerGeoDependencyComponent.builder()
            .application(application)
            .geoDependencyModule(object : GeoDependencyModule() {
                override fun providesMapFragmentFactory(): MapFragmentFactory {
                    return MapFragmentFactory { mapFragment }
                }

                override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
                    return Mockito.mock()
                }

                override fun providesScheduler(): Scheduler {
                    return Mockito.mock()
                }

                override fun providesSettingsProvider(): SettingsProvider {
                    return InMemSettingsProvider()
                }

                override fun providesWebPageService(): WebPageService {
                    return Mockito.mock()
                }

                override fun providesLocationTracker(application: Application): LocationTracker {
                    return locationTracker
                }
            })
            .build()
    }

    @Test
    fun whenLocationNotSetShouldDisplayPleaseWaitMessage() {
        val scenario = launcherRule.launchForResult(GeoPointMapActivity::class.java)
        mapFragment.ready()

        scenario.onActivity { activity: GeoPointMapActivity? ->
            Assert.assertEquals(
                activity!!.getString(
                    R.string.please_wait_long
                ), getLocationStatus(activity)
            )
        }
    }

    @Test
    fun whenLocationSetShouldDisplayStatusMessage() {
        val scenario = launcherRule.launchForResult(GeoPointMapActivity::class.java)
        mapFragment.ready()
        locationTracker.currentLocation = Location(1.0, 2.0, 3.0, 4.0f)

        scenario.onActivity { activity: GeoPointMapActivity? ->
            Assert.assertEquals(
                "Accuracy: 4 m",
                getLocationStatus(activity!!)
            )
        }
    }

    @Test
    fun shouldReturnPointFromLastLocationFix() {
        val scenario = launcherRule.launchForResult(GeoPointMapActivity::class.java)
        mapFragment.ready()

        // First location
        locationTracker.currentLocation = Location(1.0, 2.0, 3.0, 4.0f)

        // Second location
        locationTracker.currentLocation = Location(5.0, 6.0, 7.0, 8.0f)

        // When the user clicks the "Save" button, the fix location should be returned.
        scenario.onActivity { activity: GeoPointMapActivity ->
            activity.findViewById<View>(org.odk.collect.geo.R.id.accept_location).performClick()
        }

        assertThat(
            scenario.result.resultCode, equalTo(Activity.RESULT_OK)
        )
        scenario.onActivity { activity: GeoPointMapActivity ->
            val resultData = scenario.result.resultData
            assertThat(
                getReturnedSingleValue(resultData),
                equalTo(activity.formatResult(MapPoint(5.0, 6.0, 7.0, 8.0)))
            )
        }
    }

    @Test
    fun whenLocationExtraIncluded_showsMarker() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            GeoPointMapActivity::class.java
        )
        intent.putExtra(GeoPointMapActivity.EXTRA_LOCATION, MapPoint(1.0, 2.0))
        launcherRule.launch<Activity>(intent)
        mapFragment.ready()

        val markers = mapFragment.getMarkers()
        assertThat(markers.size, equalTo(1))
        assertThat(markers[0].latitude, equalTo(1.0))
        assertThat(
            markers[0].longitude,
            equalTo(2.0)
        )
    }

    @Test
    fun mapFragmentRetainMockAccuracy_isFalse() {
        launcherRule.launch(GeoPointMapActivity::class.java)
        mapFragment.ready()

        assertThat(mapFragment.isRetainMockAccuracy(), equalTo(false))
    }

    @Test
    fun passingRetainMockAccuracyExtra_updatesMapFragmentState() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            GeoPointMapActivity::class.java
        )
        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, true)
        launcherRule.launch<Activity>(intent)
        mapFragment.ready()

        assertThat(locationTracker.retainMockAccuracy, equalTo(true))

        intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, false)
        launcherRule.launch<Activity>(intent)
        mapFragment.ready()

        assertThat(locationTracker.retainMockAccuracy, equalTo(false))
    }

    @Test
    fun recreatingTheActivityWithTheLayersDialogDisplayedDoesNotCrashTheApp() {
        val scenario = launcherRule.launch(GeoPointMapActivity::class.java)
        mapFragment.ready()

        Espresso.onView(ViewMatchers.withId(org.odk.collect.geo.R.id.layer_menu)).perform(
            ViewActions.click()
        )

        scenario.recreate()
    }

    private fun getLocationStatus(activity: Activity): String {
        return activity
            .findViewById<View>(org.odk.collect.geo.R.id.status_section)
            .findViewById<TextView>(org.odk.collect.geo.R.id.location_status)
            .text.toString()
    }
}
