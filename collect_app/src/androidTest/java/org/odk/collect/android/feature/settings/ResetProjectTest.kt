package org.odk.collect.android.feature.settings

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.ResetApplicationDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R

class ResetProjectTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = chain(testDependencies)
        .around(rule)

    @Test
    fun canResetBlankForms() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("all-widgets.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertDisabled(R.string.reset_settings_button_reset)
            .clickOnString(R.string.reset_blank_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextDoesNotExist("All widgets")
    }

    @Test
    fun canResetSavedForms() {
        testDependencies.server.addForm("one-question.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            .startBlankForm("One Question")
            .fillOutAndFinalize(FormEntryPage.QuestionAndAnswer("what is your age", "34"))

            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertDisabled(R.string.reset_settings_button_reset)
            .clickOnString(R.string.reset_saved_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())

            .clickDrafts()
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun canResetProtectedSettings() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .openUserSettings()
            .uncheckServerOption()
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .checkIfServerOptionIsNotDisplayed()
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .clickOnString(R.string.reset_settings)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .checkIfServerOptionIsDisplayed()
    }

    @Test
    fun canResetUserInterfaceSettings() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText(R.string.theme_system)
            .clickOnTheme()
            .clickOnString(R.string.theme_dark)

        MainMenuPage()
            .assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText(R.string.theme_dark)
            .clickOnLanguage()
            .clickOnSelectedLanguage("español")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText("español")
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .clickOnString(R.string.reset_settings)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText(R.string.theme_system)
            .assertTextDoesNotExist(R.string.theme_dark)
            .assertText(R.string.use_device_language)
            .assertTextDoesNotExist("español")
    }

    @Test
    fun when_rotateScreen_should_resetDialogNotDisappear() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertText(R.string.reset_settings_dialog_title)
            .rotateToLandscape(ResetApplicationDialog())
            .assertText(R.string.reset_settings_dialog_title)
    }
}
