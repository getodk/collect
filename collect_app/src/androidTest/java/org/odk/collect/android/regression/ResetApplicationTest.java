package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ResetApplicationDialog;

//Issue NODK-240
public class ResetApplicationTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"))
            .around(rule);

    @Test
    public void when_rotateScreen_should_resetDialogNotDisappear() {
        //TestCase1
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnResetApplication()
                .assertText(R.string.reset_settings_dialog_title)
                .assertDisabled(R.string.reset_settings_button_reset)
                .rotateToLandscape(new ResetApplicationDialog())
                .assertText(R.string.reset_settings_dialog_title)
                .assertDisabled(R.string.reset_settings_button_reset)
                .rotateToPortrait(new ResetApplicationDialog())
                .assertText(R.string.reset_settings_dialog_title)
                .assertDisabled(R.string.reset_settings_button_reset);
    }

    @Test
    public void savedAndBlankForms_shouldBeReset() {
        //TestCase1,4
        new MainMenuPage()
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .assertText("All widgets")
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnResetApplication()
                .assertDisabled(R.string.reset_settings_button_reset)
                .clickOnString(R.string.reset_saved_forms)
                .clickOnString(R.string.reset_blank_forms)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .clickFillBlankForm()
                .assertTextDoesNotExist("All widgets")
                .pressBack(new MainMenuPage())
                .clickEditSavedForm()
                .assertTextDoesNotExist("All widgets");
    }

    @Test
    public void adminSettings_shouldBeReset() {
        //TestCase2
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .openUserSettings()
                .uncheckServerOption()
                .pressBack(new AdminSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .checkIfServerOptionIsNotDisplayed()
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .checkIfServerOptionIsDisplayed();
    }

    @Test
    public void userInterfaceSettings_shouldBeReset() {
        //TestCase3
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .assertText(R.string.theme_light)
                .clickOnTheme()
                .clickOnString(R.string.theme_dark);
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .assertText(R.string.theme_dark)
                .clickOnLanguage()
                .clickOnSelectedLanguage("español");
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .assertText("español")
                .pressBack(new GeneralSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .assertText(R.string.theme_light)
                .assertTextDoesNotExist(R.string.theme_dark)
                .assertText(R.string.use_device_language)
                .assertTextDoesNotExist("español");
    }

    @Test
    public void formManagementSettings_shouldBeReset() {
        //TestCase3
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .clickOnAutoSend()
                .clickOnString(R.string.wifi_autosend)
                .assertText(R.string.wifi_autosend)
                .clickOnDefaultToFinalized()
                .pressBack(new GeneralSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .assertMarkFinishedIsNotSelected()
                .clickSaveAndExit()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .assertText(R.string.off)
                .pressBack(new GeneralSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickJumpEndButton()
                .assertMarkFinishedIsSelected()
                .clickSaveAndExit();
    }

}
