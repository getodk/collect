package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class CatchFormCalculationExceptionsTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun typeMismatchErrorMessage_shouldBeDisplayedWhenItOccurs() {
        rule.startAtMainMenu()
            .copyForm("validate.xml")
            .startBlankForm("validate")
            .longPressOnQuestion("year")
            .removeResponse()
            .swipeToNextQuestionWithError()
            .checkIsTextDisplayedOnDialog("The value \"-01-01\" can't be converted to a date.")
    }
}
