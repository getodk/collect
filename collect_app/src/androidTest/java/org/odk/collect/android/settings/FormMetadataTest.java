package org.odk.collect.android.settings;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage;

@RunWith(AndroidJUnit4.class)
public class FormMetadataTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule("metadata.xml"));

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void settingMetadata_includesMetadataInForm() {

        //TestCase3
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickUsername()
                .inputText("Chino")
                .clickOKOnDialog()
                .clickEmail()
                .inputText("chino@whitepony.com")
                .clickOKOnDialog()
                .clickPhoneNumber()
                .inputText("664615")
                .clickOKOnDialog()
                .pressBack(new UserAndDeviceIdentitySettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("Chino", "chino@whitepony.com", "664615");
    }
}
