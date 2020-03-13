package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-239
@RunWith(AndroidJUnit4.class)
public class AdminSettingsTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE));

    @Test
    public void when_openAdminSettings_should_notCrash() {
        //TestCase1
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings()
                .assertOnPage();
    }

    @Test
    public void when_rotateOnAdminSettingsView_should_notCrash() {
        //TestCase2
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings()
                .assertOnPage()
                .rotateToLandscape(new AdminSettingsPage(rule))
                .assertOnPage()
                .rotateToPortrait(new AdminSettingsPage(rule))
                .assertOnPage();
    }

}
