package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import java.util.function.Supplier

class FormSavedSnackbarTest {
    private val rule = CollectTestRule()

    private var currentTimeMillis: Long = System.currentTimeMillis()

    private val testDependencies = object : TestDependencies() {
        override fun providesClock(): Supplier<Long> {
            return Supplier { currentTimeMillis }
        }
    }

    @get:Rule
    val copyFormChain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun whenBlankFormSavedAsDraft_displaySnackbarWithEditAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(R.string.form_saved_as_draft)
            .clickOnString(R.string.edit_form)
            .assertText("25")
            .assertText(R.string.jump_to_beginning)
            .assertText(R.string.jump_to_end)
    }

    @Test
    fun whenDraftFinalized_displaySnackbarWithViewActionThatOpensFormForEdit() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickEditSavedForm()
            .clickOnForm("One Question")
            .clickGoToEnd()
            .clickFinalize()
            .assertText(R.string.form_saved)
            .clickOnString(R.string.view_form)
            .clickOKOnDialog()
            .assertText("25")
            .assertText(R.string.jump_to_beginning)
            .assertText(R.string.jump_to_end)
    }

    @Test
    fun snackbarCanBeDismissed_andWillNotBeDisplayedAgainAfterRecreatingTheActivity() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(R.string.form_saved_as_draft)
            .closeSnackbar()
            .assertTextDoesNotExist(R.string.form_saved_as_draft)
            .rotateToLandscape(MainMenuPage())
            .assertTextDoesNotExist(R.string.form_saved_as_draft)
    }
}
