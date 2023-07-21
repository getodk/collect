package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.instancemanagement.OCTOBER_1st_2023_UTC
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
    fun beforeOCTOBER_1st_2023_UTC_whenDraftFinalized_displaySnackbarWithViewActionThatOpensFormForEdit() {
        currentTimeMillis = OCTOBER_1st_2023_UTC - 1

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
    fun afterOCTOBER_1st_2023_UTC_whenDraftFinalized_displaySnackbarWithViewActionThatOpensFormForViewOnly() {
        currentTimeMillis = OCTOBER_1st_2023_UTC + 1

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
            .assertText("25")
            .assertTextDoesNotExist(R.string.jump_to_beginning)
            .assertTextDoesNotExist(R.string.jump_to_end)
            .assertText(R.string.exit)
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
