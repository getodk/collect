package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class BulkFinalizationTest {

    val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun canBulkFinalizeDrafts() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "98"))

            .clickEditSavedForm(2)
            .clickOptionsIcon("Finalize all forms")
            .clickOnText("Finalize all forms")
            .checkIsSnackbarWithMessageDisplayed("Success! 2 forms finalized.")
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }

    @Test
    fun whenThereAreDraftsWithConstraintViolations_marksFormsAsHavingErrors() {
        rule.startAtMainMenu()
            .copyForm("two-question-required.xml")
            .startBlankForm("Two Question Required")
            .fillOut(QuestionAndAnswer("What is your name?", "Dan"))
            .pressBack(SaveOrDiscardFormDialog(MainMenuPage()))
            .clickSaveChanges()

            .startBlankForm("Two Question Required")
            .fillOutAndSave(
                QuestionAndAnswer("What is your name?", "Tim"),
                QuestionAndAnswer("What is your age?", "45", true)
            )

            .clickEditSavedForm(2)
            .clickOptionsIcon("Finalize all forms")
            .clickOnText("Finalize all forms")
            .checkIsSnackbarWithMessageDisplayed("1 forms finalized. 1 forms have errors. Address issues before finalizing all forms.")
            .assertText("Two Question Required")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun whenADraftPreviouslyHadConstraintViolations_marksFormsAsHavingErrors() {
        rule.startAtMainMenu()
            .copyForm("two-question-required.xml")
            .startBlankForm("Two Question Required")
            .fillOut(QuestionAndAnswer("What is your name?", "Dan"))
            .pressBack(SaveOrDiscardFormDialog(MainMenuPage()))
            .clickSaveChanges()

            .clickEditSavedForm(1)
            .clickOptionsIcon("Finalize all forms")
            .clickOnText("Finalize all forms")
            .checkIsSnackbarWithMessageDisplayed("1 forms have errors. Address issues before finalizing all forms.")

            .clickOptionsIcon("Finalize all forms")
            .clickOnText("Finalize all forms")
            .checkIsSnackbarWithMessageDisplayed("1 forms have errors. Address issues before finalizing all forms.")
    }

    @Test
    fun doesNotFinalizeOtherTypesOfInstance() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "98"))

            .clickEditSavedForm(1)
            .clickOptionsIcon("Finalize all forms")
            .clickOnText("Finalize all forms")
            .checkIsSnackbarWithMessageDisplayed("Success! 1 forms finalized.")
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }
}
