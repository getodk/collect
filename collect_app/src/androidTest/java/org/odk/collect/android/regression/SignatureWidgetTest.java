package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"))
            .around(rule);

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        //TestCase1
        new MainMenuPage()
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnQuestion("Signature widget")
                .clickWidgetButton()
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .checkIsTranslationDisplayed("Exit Gather Signature", "Salir Adjuntar firma")
                .assertText(R.string.keep_changes)
                .clickIgnoreChanges()
                .waitForRotationToEnd()
                .clickWidgetButton()
                .waitForRotationToEnd()
                .pressBack(new SaveOrIgnoreDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase2
        new MainMenuPage()
                .startBlankForm("All widgets")
                .clickGoToArrow()
                .clickOnText("Image widgets")
                .clickOnQuestion("Signature widget")
                .clickWidgetButton()
                .waitForRotationToEnd()
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_save_and_close)
                .clickOnId(R.id.fab_set_color)
                .clickOnString(R.string.ok)
                .clickOnId(R.id.fab_actions)
                .checkIsIdDisplayed(R.id.fab_set_color)
                .pressBack(new SaveOrIgnoreDialog<>("Gather Signature", new FormEntryPage("All widgets")))
                .clickSaveChanges()
                .waitForRotationToEnd()
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }
}
