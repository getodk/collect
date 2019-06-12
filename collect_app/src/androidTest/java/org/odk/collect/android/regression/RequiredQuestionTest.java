package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

// Issue number NODK-249
@RunWith(AndroidJUnit4.class)
public class RequiredQuestionTest extends BaseFormTest {

    @Test
    public void requiredQuestions_ShouldDisplayAsterisk() {

        //TestCase1
        EspressoTestUtilities.startBlankForm("required");
        EspressoTestUtilities.checkIsTextDisplayed("* Foo");
        EspressoTestUtilities.clickGoToIconInForm();
        EspressoTestUtilities.clickJumpEndButton();
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void requiredQuestions_ShouldDisplayCustomMessage() {

        //TestCase2
        EspressoTestUtilities.startBlankForm("required");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.checkIsToastWithMessageDisplayes("Custom required message", main);

    }
}