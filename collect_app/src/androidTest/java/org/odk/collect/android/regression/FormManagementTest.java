package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

//Issue NODK-237
@RunWith(AndroidJUnit4.class)
public class FormManagementTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("OnePageFormValid2.xml"))
            .around(new CopyFormRule("hints_textq.xml"));

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void validationUponSwipe_ShouldDisplay() {
        //TestCase7,8
        new MainMenuPage(rule)
                .startBlankForm("OnePageFormValid")
                .inputText("Bla")
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("Response length must be between 5 and 15")
                .clickOptionsIcon()
                .clickGeneralSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(R.string.constraint_behavior_on_finalize)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new FormEntryPage("OnePageFormValid", rule))
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExitWithError()
                .checkIsToastWithMessageDisplayed("Response length must be between 5 and 15");
    }

    @Test
    public void guidanceForQuestion_ShouldDisplayAlways() {
        //TestCase10
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(R.string.guidance_yes)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("hints textq")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void guidanceForQuestion_ShouldBeCollapsed() {
        //TestCase11
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(R.string.guidance_yes_collapsed)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("hints textq")
                .checkIsIdDisplayed(R.id.help_icon)
                .clickOnText("Hint 1")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

}
