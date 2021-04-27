package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.AdminSettingsPage;

//Issue NODK-239
@RunWith(AndroidJUnit4.class)
public class AdminSettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(rule);

    @Test
    public void when_openAdminSettings_should_notCrash() {
        //TestCase1
        rule.mainMenu()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .assertOnPage();
    }

    @Test
    public void when_rotateOnAdminSettingsView_should_notCrash() {
        //TestCase2
        rule.mainMenu()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .assertOnPage()
                .rotateToLandscape(new AdminSettingsPage())
                .assertOnPage()
                .rotateToPortrait(new AdminSettingsPage())
                .assertOnPage();
    }

}
