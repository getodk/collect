package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.pressBack;


// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends BaseFormTest {

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        EspressoTestUtilities.startBlankForm("All widgets");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickOnText("Image widgets");
        EspressoTestUtilities.clickOnText("Draw widget");
        EspressoTestUtilities.clickOnId(R.id.simple_button);
        pressBack();
        EspressoTestUtilities.checkIsTextDisplayed("Exit Sketch Image");
        EspressoTestUtilities.checkIsStringDisplayed(R.string.keep_changes);
        EspressoTestUtilities.clickOnString(R.string.do_not_save);
        EspressoTestUtilities.clickOnId(R.id.simple_button);
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.keep_changes);
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void setColor_ShouldSeeColorPicker() {

        //TestCase2
        EspressoTestUtilities.startBlankForm("All widgets");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickOnText("Image widgets");
        EspressoTestUtilities.clickOnText("Draw widget");
        EspressoTestUtilities.clickOnId(R.id.simple_button);
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.clickOnId(R.id.fab_set_color);
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.keep_changes);
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase3
        EspressoTestUtilities.startBlankForm("All widgets");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickOnText("Image widgets");
        EspressoTestUtilities.clickOnText("Draw widget");
        EspressoTestUtilities.clickOnId(R.id.simple_button);
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.checkIsStringDisplayed(R.string.set_color);
        EspressoTestUtilities.checkIsIdDisplayed(R.id.fab_clear);
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.checkIsStringDisplayed(R.string.set_color);
        EspressoTestUtilities.checkIsIdDisplayed(R.id.fab_save_and_close);
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.checkIsStringDisplayed(R.string.set_color);
        EspressoTestUtilities.checkIsStringDisplayed(R.string.set_color);
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.keep_changes);
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }
}