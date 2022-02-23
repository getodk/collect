package org.odk.collect.android.feature.maps

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.geo.MapProvider
import org.odk.collect.android.support.FakeClickableMapFragment
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.externalapp.ExternalAppUtils.getReturnIntent
import org.odk.collect.geo.GeoUtils
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragmentFactory

@RunWith(AndroidJUnit4::class)
class FormMapTest {

    private val mapFragment = FakeClickableMapFragment()
    private val testDependencies = object : TestDependencies() {
        override fun providesMapFragmentFactory(mapProvider: MapProvider): MapFragmentFactory {
            return object : MapFragmentFactory {
                override fun createMapFragment(context: Context): MapFragment {
                    return mapFragment
                }
            }
        }
    }
    private val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION))
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun gettingBlankFormList_showsMapIcon_onlyForFormsWithGeometry() {
        rule.startAtMainMenu()
            .copyForm(SINGLE_GEOPOINT_FORM)
            .copyForm(NO_GEOPOINT_FORM)
            .clickFillBlankForm()
            .checkMapIconDisplayedForForm("Single geopoint")
            .checkMapIconNotDisplayedForForm("basic")
    }

    @Test
    fun fillingBlankForm_addsInstanceToMap() {
        stubGeopointIntent()

        rule.startAtMainMenu()
            .copyForm(SINGLE_GEOPOINT_FORM)
            .copyForm(NO_GEOPOINT_FORM)
            .clickFillBlankForm()
            .clickOnMapIconForForm("Single geopoint")
            .clickFillBlankFormButton("Single geopoint")

            .inputText("Foo")
            .swipeToNextQuestion("Location")
            .clickWidgetButton()
            .swipeToEndScreen()
            .clickSaveAndExitBackToMap()

            .assertText(
                ApplicationProvider.getApplicationContext<Context>().resources.getString(
                    R.string.geometry_status,
                    1,
                    1
                )
            )
            .selectForm(mapFragment, 1)
            .clickEditSavedForm("Single geopoint")
            .clickOnQuestion("Name")
            .assertText("Foo")
    }

    private fun stubGeopointIntent() {
        val location = Location("gps")
        location.latitude = 125.1
        location.longitude = 10.1
        location.altitude = 5.0

        val intent = getReturnIntent(GeoUtils.formatLocationResultString(location))
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        intending(hasComponent("org.odk.collect.geo.GeoPointActivity"))
            .respondWith(result)
    }

    companion object {
        private const val SINGLE_GEOPOINT_FORM = "single-geopoint.xml"
        private const val NO_GEOPOINT_FORM = "basic.xml"
    }
}
