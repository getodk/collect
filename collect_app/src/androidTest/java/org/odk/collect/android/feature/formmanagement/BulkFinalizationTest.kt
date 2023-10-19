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
import org.odk.collect.strings.R.plurals
import org.odk.collect.strings.R.string

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

            .clickDrafts(2)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 2)
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

            .clickDrafts(2)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_partial_success, 1, 1)
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

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)

            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)
    }

    @Test
    fun doesNotFinalizeInstancesWithSavePoints() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

            .clickDrafts()
            .clickOnForm("One Question")
            .killAndReopenApp(MainMenuPage())

            .clickDrafts(false)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)
            .assertText("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun doesNotFinalizeAlreadyFinalizedInstances() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "98"))

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 1)
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }
}
