package org.odk.collect.geo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragmentFactory
import org.odk.collect.geo.maps.MapPoint
import org.odk.collect.geo.support.FakeMapFragment
import org.odk.collect.permissions.PermissionsChecker

@RunWith(AndroidJUnit4::class)
class SelectionMapFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule(R.style.Theme_MaterialComponents)

    private val map = FakeMapFragment()

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

                override fun providesReferenceLayerSettingsNavigator(): ReferenceLayerSettingsNavigator {
                    return object : ReferenceLayerSettingsNavigator {
                        override fun navigateToReferenceLayerSettings(activity: AppCompatActivity) {
                            TODO("Not yet implemented")
                        }
                    }
                }
            }).build()
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
}
