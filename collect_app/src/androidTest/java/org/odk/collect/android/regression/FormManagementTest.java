package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;

//Issue NODK-237
@RunWith(AndroidJUnit4.class)
public class FormManagementTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void guidanceForQuestion_ShouldDisplayAlways() {
        //TestCase10
        rule.startAtMainMenu()
                .copyForm("hints_textq.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("hints textq")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickFinalize();
    }

    @Test
    public void guidanceForQuestion_ShouldBeCollapsed() {
        //TestCase11
        rule.startAtMainMenu()
                .copyForm("hints_textq.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openShowGuidanceForQuestions()
                .clickOnString(org.odk.collect.strings.R.string.guidance_yes_collapsed)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .startBlankForm("hints textq")
                .checkIsIdDisplayed(R.id.help_icon)
                .clickOnText("Hint 1")
                .assertText("1 very very very very very very very very very very long text")
                .swipeToEndScreen()
                .clickFinalize();
    }

}
