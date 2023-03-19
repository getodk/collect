package org.odk.collect.android.feature.maps

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.FakeClickableMapFragment
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.externalapp.ExternalAppUtils.getReturnIntent
import org.odk.collect.geo.GeoUtils
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class FormMapTest {

    private val mapFragment = FakeClickableMapFragment()
    private val testDependencies = object : TestDependencies() {
        override fun providesMapFragmentFactory(settingsProvider: SettingsProvider): MapFragmentFactory {
            return object : MapFragmentFactory {
                override fun createMapFragment(): MapFragment {
                    return mapFragment
                }
            }
        }
    }
    private val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain(testDependencies)
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
                getApplicationContext<Context>().resources.getString(
                    R.string.select_item_count,
                    getApplicationContext<Context>().resources.getString(R.string.saved_forms),
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

        intending(hasComponent("org.odk.collect.geo.geopoint.GeoPointActivity"))
            .respondWith(result)
    }

    companion object {
        private const val SINGLE_GEOPOINT_FORM = "single-geopoint.xml"
        private const val NO_GEOPOINT_FORM = "basic.xml"
    }
}
