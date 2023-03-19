package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void setEmail_validatesEmail() {
        //TestCase1
        rule.startAtMainMenu()
                .copyForm("metadata.xml")
                .openProjectSettingsDialog()
                .clickSettings()
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
}
