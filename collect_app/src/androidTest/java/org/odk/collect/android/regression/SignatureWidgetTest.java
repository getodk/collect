package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.pressBack;


// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest extends BaseFormTest {

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        EspressoTestUtilities.startBlankForm("All widgets");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickOnText("Image widgets");
        EspressoTestUtilities.clickOnText("Signature widget");
        EspressoTestUtilities.clickSignatureButton();
        pressBack();
        EspressoTestUtilities.checkIsTextDisplayed("Exit Gather Signature");
        EspressoTestUtilities.checkIsStringDisplayed(R.string.keep_changes);
        EspressoTestUtilities.clickOnString(R.string.do_not_save);
        EspressoTestUtilities.clickSignatureButton();
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.keep_changes);
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase2
        EspressoTestUtilities.startBlankForm("All widgets");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickOnText("Image widgets");
        EspressoTestUtilities.clickOnText("Signature widget");
        EspressoTestUtilities.clickSignatureButton();
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.checkIsIdDisplayed(R.id.fab_save_and_close);
        EspressoTestUtilities.clickOnId(R.id.fab_set_color);
        EspressoTestUtilities.clickOnString(R.string.ok);
        EspressoTestUtilities.clickOnId(R.id.fab_actions);
        EspressoTestUtilities.checkIsIdDisplayed(R.id.fab_set_color);
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.keep_changes);
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }
}