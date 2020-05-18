package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

// Issue number NODK-235
@RunWith(AndroidJUnit4.class)
public class ServerOtherTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(rule);

    @Test
    public void formListPath_ShouldBeUpdated() {
        //TestCase1
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnAreaWithIndex("CheckedTextView", 2)
                .clickFormListPath()
                .addText("/formList", "/sialala")
                .clickOKOnDialog()
                .assertText("/formList/sialala");
    }

    @Test
    public void submissionsPath_ShouldBeUpdated() {
        //TestCase2
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnAreaWithIndex("CheckedTextView", 2)
                .clickSubmissionPath()
                .addText("/submission", "/blabla")
                .clickOKOnDialog()
                .assertText("/submission/blabla");
    }

}
