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
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

//Issue NODK-415
@RunWith(AndroidJUnit4.class)
public class TriggerWidgetTest extends BaseRegressionTest {
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("Automated_guidance_hint_form.xml"));

    @Test
    public void guidanceIcons_ShouldBeAlwaysShown() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(R.string.guidance_yes)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Guidance Form Sample")
                .checkIsTextDisplayed("Guidance text")
                .swipeToNextQuestion()
                .clickSaveAndExit();

    }

    @Test
    public void guidanceIcons_ShouldBeCollapsed() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(R.string.guidance_yes_collapsed)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Guidance Form Sample")
                .checkIsIdDisplayed(R.id.help_icon)
                .clickOnText("TriggerWidget")
                .checkIsTextDisplayed("Guidance text")
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }
}