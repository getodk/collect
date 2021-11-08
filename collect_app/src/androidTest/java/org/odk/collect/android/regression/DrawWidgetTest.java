package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {
        //TestCase1
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickIgnoreChanges()
                .waitForRotationToEnd()
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void setColor_ShouldSeeColorPicker() {
        //TestCase2
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .clickOnId(R.id.fab_set_color)
                .clickOnString(R.string.ok)
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {
        //TestCase3
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .assertText(R.string.set_color)
                .checkIsIdDisplayed(R.id.fab_clear)
                .clickOnId(R.id.fab_actions)
                .assertText(R.string.set_color)
                .checkIsIdDisplayed(R.id.fab_save_and_close)
                .clickOnId(R.id.fab_actions)
                .assertText(R.string.set_color)
                .assertText(R.string.set_color)
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}
