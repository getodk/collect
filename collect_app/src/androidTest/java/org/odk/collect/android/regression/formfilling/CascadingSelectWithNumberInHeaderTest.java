package org.odk.collect.android.regression.formfilling;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.regression.BaseFormTest;
import org.odk.collect.android.regression.EspressoTestUtilities;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseFormTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("numberInCSV.xml", "regression/");
    }

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
