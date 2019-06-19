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

// Issue number NODK-377
@RunWith(AndroidJUnit4.class)
public class ExternalSecondaryInstancesTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("external_select_10.xml", "regression/");
        FormLoadingUtils.copyFormToSdCard("internal_select_10.xml", "regression/");
    }

    @Test
    public void external_ShouldFillTheForm() {

        //TestCase1
        MainMenu.startBlankForm("external select 10");
        FormEntry.clickOnText("b");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("ba");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void internal_ShouldFillTheForm() {

        //TestCase2
        MainMenu.startBlankForm("internal select 10");
        FormEntry.clickOnText("c");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("ca");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();

    }
}