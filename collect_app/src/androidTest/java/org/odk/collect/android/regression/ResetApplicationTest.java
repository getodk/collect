package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.AdminSettingsPage;
import org.odk.collect.android.espressoutils.pages.GeneralSettingsPage;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

//Issue NODK-240
public class ResetApplicationTest extends BaseRegressionTest {
    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetStateRule());

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"));

    @Test
    public void savedAndBlankForms_shouldBeReset() {
        //TestCase1,4
        new MainMenuPage(main)
                .startBlankForm("All widgets")
                .clickGoToIconInForm()
                .clickJumpEndButton()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .checkIsTextDisplayed("All widgets")
                .pressBack(new MainMenuPage(main))
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnResetApplication()
                .checkIfOptionIsDisabled(R.string.reset_settings_button_reset)
                .clickOnString(R.string.reset_saved_forms)
                .clickOnString(R.string.reset_blank_forms)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage(main)
                .clickFillBlankForm()
                .checkIfTextDoesNotExist("All widgets")
                .pressBack(new MainMenuPage(main))
                .clickEditSavedForm()
                .checkIfTextDoesNotExist("All widgets");
    }

    @Test
    public void userInterfaceSettings_shouldBeReset() {
        //TestCase3
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .checkIsStringDisplayed(R.string.theme_light)
                .clickOnTheme()
                .clickOnString(R.string.theme_dark);
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .checkIsStringDisplayed(R.string.theme_dark)
                .clickOnLanguage()
                .clickOnSelectedLanguage("español");
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .checkIsTextDisplayed("español")
                .pressBack(new GeneralSettingsPage(main))
                .pressBack(new MainMenuPage(main))
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickOnUserInterface()
                .checkIsStringDisplayed(R.string.theme_light)
                .checkIfTextDoesNotExist(R.string.theme_dark)
                .checkIsStringDisplayed(R.string.use_device_language)
                .checkIfTextDoesNotExist("español");
    }

    @Test
    public void formManagementSettings_shouldBeReset() {
        //TestCase3
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .clickOnAutoSend()
                .clickOnString(R.string.wifi_autosend)
                .checkIsStringDisplayed(R.string.wifi_autosend)
                .clickOnDefaultToFinalized()
                .pressBack(new GeneralSettingsPage(main))
                .pressBack(new MainMenuPage(main))
                .startBlankForm("All widgets")
                .clickGoToIconInForm()
                .clickJumpEndButton()
                .checkIfMarkFinishedIsNotSelected()
                .clickSaveAndExit()
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .checkIsStringDisplayed(R.string.off)
                .pressBack(new GeneralSettingsPage(main))
                .pressBack(new MainMenuPage(main))
                .startBlankForm("All widgets")
                .clickGoToIconInForm()
                .clickJumpEndButton()
                .checkIfMarkFinishedIsSelected()
                .clickSaveAndExit();
    }

    @Test
    public void adminSettings_shouldBeReset() {
        //TestCase2
        new MainMenuPage(main)
                .clickOnMenu()
                .clickAdminSettings()
                .openUserSettings()
                .uncheckServerOption()
                .pressBack(new AdminSettingsPage(main))
                .pressBack(new MainMenuPage(main))
                .clickOnMenu()
                .clickGeneralSettings()
                .checkIfServerOptionIsNotDisplayed()
                .pressBack(new MainMenuPage(main))
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnResetApplication()
                .clickOnString(R.string.reset_settings)
                .clickOnString(R.string.reset_settings_button_reset)
                .clickOKOnDialog();
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings()
                .checkIfServerOptionIsDisplayed();
    }
}
