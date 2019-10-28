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
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.ScreenshotOnFailureTestRule;

import static androidx.test.espresso.Espresso.pressBack;

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
        new MainMenuPage(main).startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.waitForRotationToEnd();
        pressBack();
        FormEntry.checkIsTextDisplayed("Exit Sketch Image");
        FormEntry.checkIsStringDisplayed(R.string.keep_changes);
        FormEntry.clickOnString(R.string.do_not_save);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.waitForRotationToEnd();
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void setColor_ShouldSeeColorPicker() {

        //TestCase2
        new MainMenuPage(main).startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.clickOnId(R.id.fab_set_color);
        FormEntry.clickOnString(R.string.ok);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase3
        new MainMenuPage(main).startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_clear);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_save_and_close);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.waitForRotationToEnd();
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }
}