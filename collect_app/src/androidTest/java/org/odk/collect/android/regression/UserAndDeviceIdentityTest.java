package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.espressoutils.Settings;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import static androidx.test.espresso.Espresso.pressBack;

// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("Test.xml"));

    @Test
    public void setEmail_ShouldRequireAtSign() {
        //TestCase1
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataEmail();
        Settings.Dialog.putText("aabb");
        Settings.Dialog.clickOK();
        Settings.checkIsToastWithStringDisplayes(R.string.invalid_email_address, main);
        Settings.clickMetadataEmail();
        Settings.Dialog.putText("aa@bb");
        Settings.Dialog.clickOK();
        Settings.checkIsTextDisplayed("aa@bb");
        pressBack();
        pressBack();
        pressBack();
    }

    @Test
    public void emptyUsername_ShouldNotDisplayUsernameInForm() {

        //TestCase2
        new MainMenuPage(main).startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("");
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void setMetadataUsername_ShouldDisplayMetadataUsernameInForm() {

        //TestCase3
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.Dialog.putText("AAA");
        Settings.Dialog.clickOK();
        pressBack();
        pressBack();
        pressBack();
        new MainMenuPage(main).startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("AAA");
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void setAggregateUsername_ShouldDisplayAggregateUsernameInForm() {

        //TestCase4
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.Dialog.putText("");
        Settings.Dialog.clickOK();
        pressBack();
        pressBack();
        pressBack();

        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.openServerSettings();
        Settings.clickOnServerType();
        Settings.clickOnString(R.string.server_platform_odk_aggregate);
        Settings.clickAggregateUsername();
        Settings.Dialog.putText("BBB");
        Settings.Dialog.clickOK();
        pressBack();
        pressBack();
        new MainMenuPage(main).startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("BBB");
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void setBothUsernames_ShouldDisplayMetadataUsernameInForm() {

        //TestCase5
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.clickUserAndDeviceIdentity();
        Settings.clickFormMetadata();
        Settings.clickMetadataUsername();
        Settings.Dialog.putText("CCC");
        Settings.Dialog.clickOK();
        pressBack();
        pressBack();
        pressBack();
        
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.openServerSettings();
        Settings.clickOnServerType();
        Settings.clickOnString(R.string.server_platform_odk_aggregate);
        Settings.clickAggregateUsername();
        Settings.Dialog.putText("DDD");
        Settings.Dialog.clickOK();
        pressBack();
        pressBack();
        new MainMenuPage(main).startBlankForm("Test");
        FormEntry.checkIsDisplayedInTextClassAndSwipe("CCC");
        FormEntry.clickSaveAndExit();
    }
}