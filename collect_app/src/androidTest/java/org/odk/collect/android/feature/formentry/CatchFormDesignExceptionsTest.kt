package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class CatchFormDesignExceptionsTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test // https://github.com/getodk/collect/issues/4750
    fun whenFormHasDesignErrors_explanationDialogShouldBeDisplayedAndTheFormShouldBeClosed() {
        rule.startAtMainMenu()
            .copyForm("form_design_error.xml")
            .clickFillBlankForm()
            .clickOnForm("Relevance and calculate loop")
            .answerQuestion(1, "B")
            .scrollToAndAssertText("third")
            .answerQuestion(2, "C")
            // Answering C above triggers a recomputation round which resets fullName to name.
            // They're then equal which makes the third question non-relevant. Trying to change the
            // value of a non-relevant node throws an exception.
            .answerQuestion(2, "D")
            .assertText(R.string.update_widgets_error)
            .clickOKOnDialog()
            .assertOnPage(MainMenuPage())
    }
}
