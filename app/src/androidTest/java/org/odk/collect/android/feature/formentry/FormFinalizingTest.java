package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class FormFinalizingTest {

    private static final String FORM = "one-question.xml";

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);


    @Test
    public void fillingForm_andPressingSaveAndExit_finalizesForm() {
        rule.startAtMainMenu()
                .copyForm(FORM)
                .assertNumberOfFinalizedForms(0)
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .assertNumberOfFinalizedForms(1);
    }

    @Test
    public void fillingForm_andUncheckingFinalize_andPressingSaveAndExit_doesNotFinalizesForm() {
        rule.startAtMainMenu()
                .copyForm(FORM)
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
        rule.startAtMainMenu()
                .copyForm(FORM)
                .assertNumberOfFinalizedForms(0)
                .startBlankForm("One Question")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickSaveChanges()
                .assertNumberOfFinalizedForms(0);
    }
}
