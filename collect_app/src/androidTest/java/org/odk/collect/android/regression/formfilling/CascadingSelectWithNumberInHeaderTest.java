package org.odk.collect.android.regression.formfilling;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.regression.BaseFormTest;
import org.odk.collect.android.regression.EspressoTestUtilities;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseFormTest {

    @Test
    public void fillForm_ShouldFillFormWithNumberInCsvHeader() {

        EspressoTestUtilities.startBlankForm("numberInCSV");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickOnText("Venda de animais");
        EspressoTestUtilities.checkIsTextDisplayed("1a");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickOnText("Vendas agrícolas");
        EspressoTestUtilities.checkIsTextDisplayed("2a");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickOnText("Pensão");
        EspressoTestUtilities.checkIsTextDisplayed("3a");
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.swipeToNextQuestion();
        EspressoTestUtilities.clickSaveAndExit();

    }
}
