package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.espressoutils.Settings;

import static androidx.test.espresso.Espresso.pressBack;

//Issue NODK-241
@RunWith(AndroidJUnit4.class)
public class UserSettingsTest extends BaseRegressionTest {

    @Test
    public void typeOption_ShouldNotBeVisible() {
        //TestCase1
        MainMenu.clickOnMenu();
        MainMenu.clickAdminSettings();
        Settings.openUserSettings();
        FormEntry.checkIfTextDoesNotExist("Type");
        FormEntry.checkIfTextDoesNotExist("Submission transport");
        FormEntry.checkIsTextDisplayed("Server");
    }

    @Test
    public void uncheckedSettings_ShouldNotBeVisibleInGeneralSettings() {
        //TestCase4
        MainMenu.clickOnMenu();
        MainMenu.clickAdminSettings();
        Settings.openUserSettings();
        Settings.uncheckAllUsetSettings();
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.checkIfStringDoesNotExist(R.string.server);
        Settings.checkIfStringDoesNotExist(R.string.client);
        Settings.checkIfStringDoesNotExist(R.string.maps);
        Settings.checkIfStringDoesNotExist(R.string.form_management_preferences);
        Settings.checkIfStringDoesNotExist(R.string.user_and_device_identity_title);
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickAdminSettings();
        Settings.openGeneralSettingsFromAdminSettings();
        Settings.checkIfAreaWithKeyIsDisplayed("protocol");
        Settings.checkIfAreaWithKeyIsDisplayed("user_interface");
        Settings.checkIfAreaWithKeyIsDisplayed("maps");
        Settings.checkIfAreaWithKeyIsDisplayed("form_management");
        Settings.checkIfAreaWithKeyIsDisplayed("user_and_device_identity");
        pressBack();
        pressBack();
        Settings.resetSettings();
    }

    @Test
    public void showGuidance_shouldBehidden() {
        //TestCase5
        MainMenu.clickOnMenu();
        MainMenu.clickAdminSettings();
        Settings.openUserSettings();
        Settings.uncheckUserSettings("guidance_hint");
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.openFormManagement();
        Settings.checkIfStringDoesNotExist(R.string.guidance_hint_title);
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickAdminSettings();
        Settings.openUserSettings();
        Settings.uncheckAllUsetSettings();
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.openFormManagement();
        FormEntry.checkIsStringDisplayed(R.string.guidance_hint_title);
        pressBack();
        pressBack();
        Settings.resetSettings();
    }
}
