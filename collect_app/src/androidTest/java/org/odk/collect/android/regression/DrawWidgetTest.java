package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDrawingDialog;

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
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.draw_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickDiscardChanges()
                .waitForRotationToEnd()
                .clickOnId(R.id.draw_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }

    @Test
    public void setColor_ShouldSeeColorPicker() {
        //TestCase2
        rule.startAtMainMenu()
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.draw_button)
                .waitForRotationToEnd()
                .clickOnId(org.odk.collect.draw.R.id.fab_actions)
                .clickOnId(org.odk.collect.draw.R.id.fab_set_color)
                .clickOnString(org.odk.collect.strings.R.string.ok)
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {
        //TestCase3
        rule.startAtMainMenu()
                .copyForm("all-widgets.xml")
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.draw_button)
                .waitForRotationToEnd()
                .clickOnId(org.odk.collect.draw.R.id.fab_actions)
                .assertText(org.odk.collect.strings.R.string.set_color)
                .checkIsIdDisplayed(org.odk.collect.draw.R.id.fab_clear)
                .clickOnId(org.odk.collect.draw.R.id.fab_actions)
                .assertText(org.odk.collect.strings.R.string.set_color)
                .checkIsIdDisplayed(org.odk.collect.draw.R.id.fab_save_and_close)
                .clickOnId(org.odk.collect.draw.R.id.fab_actions)
                .assertText(org.odk.collect.strings.R.string.set_color)
                .assertText(org.odk.collect.strings.R.string.set_color)
                .pressBack(new SaveOrIgnoreDrawingDialog<>("Sketch Image", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }
}
