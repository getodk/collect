package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class BigFormTest {
    private var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun bigForm_ShouldBeFilledSuccessfully() {
        rule.startAtMainMenu()
            .copyForm("nigeria-wards.xml")
            .startBlankForm("Nigeria Wards")
            .assertQuestion("State")
            .openSelectMinimalDialog()
            .selectItem("Adamawa")
            .swipeToNextQuestion("LGA", true)
            .openSelectMinimalDialog()
            .selectItem("Ganye")
            .swipeToNextQuestion("Ward", true)
            .openSelectMinimalDialog()
            .selectItem("Jaggu")
            .swipeToNextQuestion("Comments")
            .swipeToEndScreen()
            .clickFinalize()
    }
}
