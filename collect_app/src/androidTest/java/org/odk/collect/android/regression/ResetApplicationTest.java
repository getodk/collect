package org.odk.collect.android.regression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.AccessControlPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.ResetApplicationDialog;

//Issue NODK-240
public class ResetApplicationTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void when_rotateScreen_should_resetDialogNotDisappear() {
        //TestCase1
        rule.startAtMainMenu()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickProjectManagement()
                .clickOnResetApplication()
                .assertText(org.odk.collect.strings.R.string.reset_settings_dialog_title)
                .assertDisabled(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .rotateToLandscape(new ResetApplicationDialog())
                .assertText(org.odk.collect.strings.R.string.reset_settings_dialog_title)
                .assertDisabled(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .rotateToPortrait(new ResetApplicationDialog())
                .assertText(org.odk.collect.strings.R.string.reset_settings_dialog_title)
                .assertDisabled(org.odk.collect.strings.R.string.reset_settings_button_reset);
    }

    @Test
    public void savedAndBlankForms_shouldBeReset() {
        //TestCase1,4
        rule.startAtMainMenu()
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAsDraft()
                .clickDrafts()
                .assertText("All widgets")
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .clickProjectManagement()
                .clickOnResetApplication()
                .assertDisabled(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .clickOnString(org.odk.collect.strings.R.string.reset_saved_forms)
                .clickOnString(org.odk.collect.strings.R.string.reset_blank_forms)
                .clickOnString(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .clickFillBlankForm()
                .assertTextDoesNotExist("All widgets")
                .pressBack(new MainMenuPage())
                .clickDrafts()
                .assertTextDoesNotExist("All widgets");
    }

    @Test
    public void adminSettings_shouldBeReset() {
        //TestCase2
        rule.startAtMainMenu()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickAccessControl()
                .openUserSettings()
                .uncheckServerOption()
                .pressBack(new AccessControlPage())
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .checkIfServerOptionIsNotDisplayed()
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .clickProjectManagement()
                .clickOnResetApplication()
                .clickOnString(org.odk.collect.strings.R.string.reset_settings)
                .clickOnString(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .checkIfServerOptionIsDisplayed();
    }

    @Test
    public void userInterfaceSettings_shouldBeReset() {
        //TestCase3
        rule.startAtMainMenu()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .assertText(org.odk.collect.strings.R.string.theme_system)
                .clickOnTheme()
                .clickOnString(org.odk.collect.strings.R.string.theme_dark);

        new MainMenuPage()
                .assertOnPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .assertText(org.odk.collect.strings.R.string.theme_dark)
                .clickOnLanguage()
                .clickOnSelectedLanguage("español")

                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .assertText("español")
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .clickProjectManagement()
                .clickOnResetApplication()
                .clickOnString(org.odk.collect.strings.R.string.reset_settings)
                .clickOnString(org.odk.collect.strings.R.string.reset_settings_button_reset)
                .clickOKOnDialog(new MainMenuPage())

                .openProjectSettingsDialog()
                .clickSettings()
                .clickOnUserInterface()
                .assertText(org.odk.collect.strings.R.string.theme_system)
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.theme_dark)
                .assertText(org.odk.collect.strings.R.string.use_device_language)
                .assertTextDoesNotExist("español");
    }
}
