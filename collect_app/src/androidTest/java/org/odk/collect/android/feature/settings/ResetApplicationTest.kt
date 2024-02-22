package org.odk.collect.android.feature.settings

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.ResetApplicationDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.strings.R

class ResetApplicationTest {
    private var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun when_rotateScreen_should_resetDialogNotDisappear() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetApplication()
            .assertText(R.string.reset_settings_dialog_title)
            .assertDisabled(R.string.reset_settings_button_reset)
            .rotateToLandscape(ResetApplicationDialog())
            .assertText(R.string.reset_settings_dialog_title)
            .assertDisabled(R.string.reset_settings_button_reset)
            .rotateToPortrait(ResetApplicationDialog())
            .assertText(R.string.reset_settings_dialog_title)
            .assertDisabled(R.string.reset_settings_button_reset)
    }

    @Test
    fun savedAndBlankForms_shouldBeReset() {
        rule.startAtMainMenu()
            .copyForm("all-widgets.xml")
            .startBlankForm("All widgets")
            .clickGoToArrow()
            .clickJumpEndButton()
            .clickSaveAsDraft()
            .clickDrafts()
            .assertText("All widgets")
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetApplication()
            .assertDisabled(R.string.reset_settings_button_reset)
            .clickOnString(R.string.reset_saved_forms)
            .clickOnString(R.string.reset_blank_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())
            .clickFillBlankForm()
            .assertTextDoesNotExist("All widgets")
            .pressBack(MainMenuPage())
            .clickDrafts(false)
            .assertTextDoesNotExist("All widgets")
    }

    @Test
    fun adminSettings_shouldBeReset() {
        rule.startAtMainMenu()
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
            .clickOnResetApplication()
            .clickOnString(R.string.reset_settings)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .checkIfServerOptionIsDisplayed()
    }

    @Test
    fun userInterfaceSettings_shouldBeReset() {
        rule.startAtMainMenu()
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
            .clickOnResetApplication()
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
}
