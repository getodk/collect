package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage

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
            .clickOnForm("form_design_error")
            .openSelectMinimalDialog()
            .clickOnText("A")
            .assertText(R.string.update_widgets_error)
            .clickOKOnDialog()
            .assertOnPage(MainMenuPage())
    }
}
