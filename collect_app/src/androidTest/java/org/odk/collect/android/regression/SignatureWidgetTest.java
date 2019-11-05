package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.ScreenshotOnFailureTestRule;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
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
        new MainMenuPage(main)
                .startBlankForm("All widgets")
                .clickGoToIconInForm()
                .clickOnText("Image widgets")
                .clickOnText("Signature widget")
                .clickSignatureButton()
                .waitForRotationToEnd()
                .simplePressBack()
                .checkIsTranslationDisplayed("Exit Gather Signature", "Salir Adjuntar firma")
                .checkIsStringDisplayed(R.string.keep_changes)
                .clickOnString(R.string.do_not_save)
                .waitForRotationToEnd()
                .clickSignatureButton()
                .waitForRotationToEnd()
                .simplePressBack()
                .clickOnString(R.string.keep_changes)
                .waitForRotationToEnd()
                .clickGoToIconInForm()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase2
        new MainMenuPage(main)
                .startBlankForm("All widgets")
                .clickGoToIconInForm()
                .clickOnText("Image widgets")
                .clickOnText("Signature widget")
                .clickSignatureButton()
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_save_and_close)
                .clickOnId(R.id.fab_set_color)
                .clickOnString(R.string.ok)
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_set_color)
                .simplePressBack()
                .clickOnString(R.string.keep_changes)
                .waitForRotationToEnd()
                .clickGoToIconInForm()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}