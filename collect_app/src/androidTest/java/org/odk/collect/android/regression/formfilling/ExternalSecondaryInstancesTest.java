package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

// Issue number NODK-377
@RunWith(AndroidJUnit4.class)
public class ExternalSecondaryInstancesTest extends BaseFormTest {

    @Test
    public void external_ShouldFillTheForm() {

        //TestCase1
        EspressoTestUtilities.startBlankForm("external select 10");
        EspressoTestUtilities.clickOnText("b");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickOnText("ba");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void internal_ShouldFillTheForm() {

        //TestCase2
        EspressoTestUtilities.startBlankForm("internal select 10");
        EspressoTestUtilities.clickOnText("c");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickOnText("ca");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickSaveAndExit();

    }
}