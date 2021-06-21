package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormMetadataTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    /**
     * CollectSetGeopointAction` instances will be serialized as part of a form definition cache.
     * It's easy to introduce deserialization problems without realizing as they'll only
     * appear on the second load.
     */
    @Test
    fun startGeopoint_withCachedFormDefinition_doesNotCauseError() {
        rule.startAtMainMenu()
            .copyForm("start-geopoint.xml")

            // Open form once to cache definition
            .clickFillBlankForm()
            .clickOnEmptyForm("start-geopoint")
            .clickSaveAndExit()

            // Load with cached definition
            .clickFillBlankForm()
            .clickOnEmptyForm("start-geopoint")
    }
}
