package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

// Issue number NODK-251
@RunWith(AndroidJUnit4.class)
public class FormValidationTest extends BaseFormTest {

    @Test
    public void invalidAnswer_ShouldDisplayAllQuestionsOnOnePage() {

        EspressoTestUtilities.startBlankForm("OnePageFormShort");
        EspressoTestUtilities.putTextOnIndex(0, "A");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();
        EspressoTestUtilities.checkIsToastWithMessageDisplayes("Response length must be between 5 and 15", main);
        EspressoTestUtilities.checkIsTextDisplayed("Integer");
        EspressoTestUtilities.putTextOnIndex(0, "Aaaaa");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void openHierarchyView_ShouldSeeShortForms() {

        //TestCase3
        EspressoTestUtilities.startBlankForm("OnePageFormShort");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.checkIsTextDisplayed("YY MM");
        EspressoTestUtilities.checkIsTextDisplayed("YY");

    }
}