package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class FormFinalizingTest {

    private static final String FORM = "one-question.xml";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(FORM));

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void fillingForm_andPressingSaveAndExit_finalizesForm() {
        new MainMenuPage(rule)
                .assertNumberOfFinalizedForms(0)
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .assertNumberOfFinalizedForms(1);
    }

    @Test
    public void fillingForm_andUncheckingFinalize_andPressingSaveAndExit_doesNotFinalizesForm() {
        new MainMenuPage(rule)
                .assertNumberOfFinalizedForms(0)
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickMarkAsFinalized()
                .assertMarkFinishedIsNotSelected()
                .clickSaveAndExit()
                .assertNumberOfFinalizedForms(0);
    }

    @Test
    public void fillingForm_andPressingBack_andPressingSave_doesNotFinalizesForm() {
        new MainMenuPage(rule)
                .assertNumberOfFinalizedForms(0)
                .startBlankForm("One Question")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage(rule), rule))
                .clickSaveChanges()
                .assertNumberOfFinalizedForms(0);
    }
}
