package org.odk.collect.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.EditSavedFormPage
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.strings.R.plurals
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class BulkFinalizationTest {

    val testDependencies = TestDependencies()
    val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun canBulkFinalizeDrafts() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "98"))

            .clickDrafts(2)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 2)
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }

    @Test
    fun whenThereAreDraftsWithConstraintViolations_marksFormsAsHavingErrors() {
        rule.withProject("http://example.com")
            .copyForm("two-question-required.xml", "example.com")
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
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_partial_success, 1, 1)
            .assertText("Two Question Required")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun whenADraftPreviouslyHadConstraintViolations_marksFormsAsHavingErrors() {
        rule.withProject("http://example.com")
            .copyForm("two-question-required.xml", "example.com")
            .startBlankForm("Two Question Required")
            .fillOut(QuestionAndAnswer("What is your name?", "Dan"))
            .pressBack(SaveOrDiscardFormDialog(MainMenuPage()))
            .clickSaveChanges()

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)

            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)
    }

    @Test
    fun doesNotFinalizeInstancesWithSavePoints() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

            .clickDrafts()
            .clickOnForm("One Question")
            .killAndReopenApp(MainMenuPage())

            .clickDrafts()
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_unsupported, 0)
            .assertText("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun doesNotFinalizeInstancesFromEncryptedForms() {
        rule.withProject("http://example.com")
            .copyForm("encrypted.xml", "example.com")
            .startBlankForm("encrypted")
            .swipeToEndScreen()
            .clickSaveAsDraft()

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_unsupported, 0)
            .assertText("encrypted")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun doesNotFinalizeAlreadyFinalizedInstances() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "98"))

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 1)
            .assertTextDoesNotExist("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }

    @Test
    fun whenAutoSendIsEnabled_draftsAreSentAfterFinalizing() {
        val mainMenuPage = rule.withProject(testDependencies.server.url)
            .enableAutoSend()

            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.finalize, EditSavedFormPage())
            .pressBack(MainMenuPage())

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.clickViewSentForm(1)
            .assertText("One Question")

        assertThat(testDependencies.server.submissions.size, equalTo(1))
    }

    @Test
    fun canCancel() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))

            .clickDrafts(1)
            .clickOptionsIcon(string.finalize_all_forms)
            .clickOnString(string.finalize_all_forms)
            .clickOnButtonInDialog(string.cancel, EditSavedFormPage())
            .assertText("One Question")
            .pressBack(MainMenuPage())

            .assertNumberOfEditableForms(1)
    }
}
