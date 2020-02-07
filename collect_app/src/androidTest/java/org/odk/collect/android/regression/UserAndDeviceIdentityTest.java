package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

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
            .around(new CopyFormRule("metadata.xml"));

    @Test
    public void setEmail_validatesEmail() {
        //TestCase1
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickEmail()
                .inputText("aabb")
                .clickOKOnDialog()
                .checkIsToastWithMessageDisplayed(R.string.invalid_email_address)
                .clickEmail()
                .inputText("aa@bb")
                .clickOKOnDialog()
                .assertText("aa@bb");
    }

    @Test
    public void setAggregateUsername_ShouldDisplayAggregateUsernameInForm() {
        //TestCase4
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickUsername()
                .inputText("")
                .clickOKOnDialog()
                .pressBack(new UserAndDeviceIdentitySettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnString(R.string.server_platform_odk_aggregate)
                .clickAggregateUsername()
                .inputText("BBB")
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("BBB");
    }

    @Test
    public void setBothUsernames_ShouldDisplayMetadataUsernameInForm() {
        //TestCase5
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickUsername()
                .inputText("CCC")
                .clickOKOnDialog()
                .pressBack(new UserAndDeviceIdentitySettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnString(R.string.server_platform_odk_aggregate)
                .clickAggregateUsername()
                .inputText("DDD")
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("CCC");
    }
}