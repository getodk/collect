package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

class FormSaveTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

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
            .assertText(org.odk.collect.strings.R.string.form_saved_as_draft)
            .clickOnString(org.odk.collect.strings.R.string.edit_form)
            .assertText("25")
            .assertText(org.odk.collect.strings.R.string.jump_to_beginning)
            .assertText(org.odk.collect.strings.R.string.jump_to_end)
    }

    @Test
    fun whenDraftFinalized_displaySnackbarWithViewAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickDrafts()
            .clickOnForm("One Question")
            .clickGoToEnd()
            .clickFinalize()
            .assertText(org.odk.collect.strings.R.string.form_saved)
            .clickOnString(org.odk.collect.strings.R.string.view_form)
            .assertText("25")
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.jump_to_beginning)
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.jump_to_end)
            .assertText(org.odk.collect.strings.R.string.exit)
    }

    @Test
    fun snackbarCanBeDismissed_andWillNotBeDisplayedAgainAfterRecreatingTheActivity() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(org.odk.collect.strings.R.string.form_saved_as_draft)
            .closeSnackbar()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.form_saved_as_draft)
            .rotateToLandscape(MainMenuPage())
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.form_saved_as_draft)
    }

    @Test
    fun whenFormDeletedDuringFilling_displayErrorOnAttemptToSave() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .startBlankForm("One Question")
            .also { testDependencies.scheduler.runDeferredTasks() }
            .clickSaveWithError("Sorry, form save failed! Form can't be found.")
            .swipeToEndScreen()
            .clickSaveAsDraftWithError("Sorry, form save failed! Form can't be found.")
            .clickFinalizeWithError("Sorry, form save failed! Form can't be found.")
    }
}
