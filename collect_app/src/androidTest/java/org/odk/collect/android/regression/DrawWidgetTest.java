package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.ScreenshotOnFailureTestRule;

// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"));

    @Rule
    public TestRule screenshotFailRule = new ScreenshotOnFailureTestRule();

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets", rule), rule))
                .clickIgnoreChanges()
                .waitForRotationToEnd()
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets", rule), rule))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void setColor_ShouldSeeColorPicker() {

        //TestCase2
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .clickOnId(R.id.fab_set_color)
                .clickOnString(R.string.ok)
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets", rule), rule))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase3
        new MainMenuPage(rule)
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnText("Draw widget")
                .clickOnId(R.id.simple_button)
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .checkIsStringDisplayed(R.string.set_color)
                .checkIsIdDisplayed(R.id.fab_clear)
                .clickOnId(R.id.fab_actions)
                .checkIsStringDisplayed(R.string.set_color)
                .checkIsIdDisplayed(R.id.fab_save_and_close)
                .clickOnId(R.id.fab_actions)
                .checkIsStringDisplayed(R.string.set_color)
                .checkIsStringDisplayed(R.string.set_color)
                .pressBack(new SaveOrIgnoreDialog<>("Sketch Image", new FormEntryPage("All widgets", rule), rule))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}