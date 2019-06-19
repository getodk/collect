package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.espressoutils.Settings;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;

import static androidx.test.espresso.Espresso.pressBack;


// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("Test.xml", "regression/");
    }

    @Test
    public void setEmail_ShouldRequireAtSign() {

        //TestCase1
        Settings.resetSettings();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataEmail();
        Settings.putText("aabb");
        pressBack();
        Settings.clickOnString(R.string.ok);
        Settings.checkIsToastWithStringDisplayes(R.string.invalid_email_address, main);
        Settings.clickMetadataEmail();
        Settings.putText("aa@bb");
        pressBack();
        Settings.clickOnString(R.string.ok);
        Settings.checkIsTextDisplayed("aa@bb");
        pressBack();
        pressBack();
        pressBack();

    }

    @Test
    public void emptyUsername_ShouldNotDisplayUsernameInForm() {

        //TestCase2
        Settings.resetSettings();
        MainMenu.startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("");
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void setMetadataUsername_ShouldDisplayMetadataUsernameInForm() {

        //TestCase3
        Settings.resetSettings();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.putText("AAA");
        Settings.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        MainMenu.startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("AAA");
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void setAggregateUsername_ShouldDisplayAggregateUsernameInForm() {

        //TestCase4
        Settings.resetSettings();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.putText("");
        Settings.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.openServerSettings();
        Settings.clickOnServerType();
        Settings.clickOnString(R.string.server_platform_odk_aggregate);
        Settings.clickAggregateUsername();
        Settings.putText("BBB");
        Settings.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        MainMenu.startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("BBB");
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void setBothUsernames_ShouldDisplayMetadataUsernameInForm() {

        //TestCase5
        Settings.resetSettings();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.putText("CCC");
        Settings.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        MainMenu.clickOnMenu();
        MainMenu.clickGeneralSettings();
        Settings.openServerSettings();
        Settings.clickOnServerType();
        Settings.clickOnString(R.string.server_platform_odk_aggregate);
        Settings.clickAggregateUsername();
        Settings.putText("DDD");
        Settings.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        MainMenu.startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("CCC");
        FormEntry.clickSaveAndExit();

    }
}