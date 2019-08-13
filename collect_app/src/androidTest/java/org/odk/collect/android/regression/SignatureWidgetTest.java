package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.ScreenshotOnFailureTestRule;

import static androidx.test.espresso.Espresso.pressBack;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
@Ignore("https://github.com/opendatakit/collect/issues/3205")
public class SignatureWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"));

    @Rule
    public TestRule screenshotFailRule = new ScreenshotOnFailureTestRule();

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