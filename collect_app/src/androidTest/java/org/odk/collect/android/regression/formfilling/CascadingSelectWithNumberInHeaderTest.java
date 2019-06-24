package org.odk.collect.android.regression.formfilling;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.regression.BaseRegressionTest;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;
import java.util.Collections;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("numberInCSV.xml", "regression/", Collections.singletonList("itemSets.csv"));
    }

    @Test
    public void fillForm_ShouldFillFormWithNumberInCsvHeader() {
        MainMenu.startBlankForm("numberInCSV");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Venda de animais");
        FormEntry.checkIsTextDisplayed("1a");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Vendas agrícolas");
        FormEntry.checkIsTextDisplayed("2a");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Pensão");
        FormEntry.checkIsTextDisplayed("3a");
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

    }
}
