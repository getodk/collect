package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

// Issue number NODK-251
@RunWith(AndroidJUnit4.class)
public class FormValidationTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("OnePageFormShort.xml"));

    @Test
    public void invalidAnswer_ShouldDisplayAllQuestionsOnOnePage() {

        new MainMenuPage(rule)
                .startBlankForm("OnePageFormShort")
                .putTextOnIndex(0, "A")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExitWhenValidationErrorIsExpected()
                .checkIsToastWithMessageDisplayed("Response length must be between 5 and 15")
                .checkIsTextDisplayed("Integer")
                .putTextOnIndex(0, "Aaaaa")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExit();
    }

    @Test
    public void openHierarchyView_ShouldSeeShortForms() {

        //TestCase3
        new MainMenuPage(rule)
                .startBlankForm("OnePageFormShort")
                .clickGoToArrow()
                .checkIsTextDisplayed("YY MM")
                .checkIsTextDisplayed("YY")
                .pressBack(new FormEntryPage("OnePageFormShort", rule))
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("OnePageFormShort", new MainMenuPage(rule), rule))
                .clickIgnoreChanges();
    }
}