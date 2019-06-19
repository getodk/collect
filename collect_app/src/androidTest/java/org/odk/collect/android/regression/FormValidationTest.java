package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;

// Issue number NODK-251
@RunWith(AndroidJUnit4.class)
public class FormValidationTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("OnePageFormShort.xml", "regression/");
    }

    @Test
    public void invalidAnswer_ShouldDisplayAllQuestionsOnOnePage() {

        MainMenu.startBlankForm("OnePageFormShort");
        FormEntry.putTextOnIndex(0, "A");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Response length must be between 5 and 15", main);
        FormEntry.checkIsTextDisplayed("Integer");
        FormEntry.putTextOnIndex(0, "Aaaaa");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void openHierarchyView_ShouldSeeShortForms() {

        //TestCase3
        MainMenu.startBlankForm("OnePageFormShort");
        FormEntry.clickGoToIconInForm();
        FormEntry.checkIsTextDisplayed("YY MM");
        FormEntry.checkIsTextDisplayed("YY");

    }
}