package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;

// Issue number NODK-249
@RunWith(AndroidJUnit4.class)
public class RequiredQuestionTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("requiredJR275.xml", "regression/");
    }

    @Test
    public void requiredQuestions_ShouldDisplayAsterisk() {

        //TestCase1
        MainMenu.startBlankForm("required");
        FormEntry.checkIsTextDisplayed("* Foo");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();

    }

    @Test
    public void requiredQuestions_ShouldDisplayCustomMessage() {

        //TestCase2
        MainMenu.startBlankForm("required");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsToastWithMessageDisplayes("Custom required message", main);

    }
}