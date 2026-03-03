package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.PageComposeRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.AssertionFramework

@RunWith(AndroidJUnit4::class)
class FormEndTest {

    private val rule = CollectTestRule()
    private val pageComposeRule = PageComposeRule()

    @get:Rule
    val copyFormChain: RuleChain = chain()
        .around(pageComposeRule.composeRule)
        .around(pageComposeRule)
        .around(rule)

    @Test
    fun fillingForm_andPressingFinalize_finalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("what is your age", "52"))
            .assertNumberOfEditableForms(0)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun fillingForm_andPressingSaveAsDraft_savesACompleteDraft() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertNumberOfFinalizedForms(0)

            .clickDrafts(1)
            .assertText(string.draft_no_errors, assertionFramework = AssertionFramework.COMPOSE)
            .assertTextDoesNotExist(string.draft_errors, assertionFramework = AssertionFramework.COMPOSE)
    }

    @Test
    fun fillingForm_andPressingSaveAsDraft_whenThereAreViolatedConstraints_savesAIncompleteDraft() {
        rule.startAtMainMenu()
            .copyForm("two-question-required.xml")
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("Two Question Required")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickSaveAsDraft()
            .assertNumberOfFinalizedForms(0)

            .clickDrafts(1)
            .assertText(string.draft_errors, assertionFramework = AssertionFramework.COMPOSE)
            .assertTextDoesNotExist(string.draft_no_errors, assertionFramework = AssertionFramework.COMPOSE)
    }

    @Test
    fun disablingSaveAsDraftInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnSaveAsDraftInFormEnd()
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .assertTextDoesNotExist(string.save_as_draft)
    }

    @Test
    fun disablingFinalizeInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(string.finalize)
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .assertTextDoesNotExist(string.finalize)
    }

    companion object {
        private const val FORM = "one-question.xml"
    }
}
