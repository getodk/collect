package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.test.FormLoadingUtils;

import java.io.IOException;

import static androidx.test.espresso.Espresso.pressBack;


// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest extends BaseRegressionTest {

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        FormLoadingUtils.copyFormToSdCard("All_widgets.xml", "regression/");
    }

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Signature widget");
        FormEntry.clickSignatureButton();
        pressBack();
        FormEntry.checkIsTextDisplayed("Exit Gather Signature");
        FormEntry.checkIsStringDisplayed(R.string.keep_changes);
        FormEntry.clickOnString(R.string.do_not_save);
        FormEntry.clickSignatureButton();
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase2
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Signature widget");
        FormEntry.clickSignatureButton();
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsIdDisplayed(R.id.fab_save_and_close);
        FormEntry.clickOnId(R.id.fab_set_color);
        FormEntry.clickOnString(R.string.ok);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsIdDisplayed(R.id.fab_set_color);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }
}