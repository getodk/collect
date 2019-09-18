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
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.openFormManagement();
        Settings.openShowGuidanceForQuestions();
        Settings.clickOnString(R.string.guidance_yes);
        pressBack();
        pressBack();
        new MainMenuPage(main).startBlankForm("Guidance Form Sample");
        FormEntry.checkIsTextDisplayed("Guidance text");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void guidanceIcons_ShouldBeCollapsed() {
        new MainMenuPage(main)
                .clickOnMenu()
                .clickGeneralSettings();

        Settings.openFormManagement();
        Settings.openShowGuidanceForQuestions();
        Settings.clickOnString(R.string.guidance_yes_collapsed);
        pressBack();
        pressBack();
        new MainMenuPage(main).startBlankForm("Guidance Form Sample");
        FormEntry.checkIsIdDisplayed(R.id.help_icon);
        FormEntry.clickOnText("TriggerWidget");
        FormEntry.checkIsTextDisplayed("Guidance text");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
    }
}