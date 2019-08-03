package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.pressBack;

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
        pressBack();
        closeSoftKeyboard();
        pressBack();
        FormEntry.clickIgnoreChanges();
    }
}