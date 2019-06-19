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


// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends BaseRegressionTest {

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
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        pressBack();
        FormEntry.checkIsTextDisplayed("Exit Sketch Image");
        FormEntry.checkIsStringDisplayed(R.string.keep_changes);
        FormEntry.clickOnString(R.string.do_not_save);
        FormEntry.clickOnId(R.id.simple_button);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void setColor_ShouldSeeColorPicker() {

        //TestCase2
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.clickOnId(R.id.fab_set_color);
        FormEntry.clickOnString(R.string.ok);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase3
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_clear);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_save_and_close);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();

    }
}