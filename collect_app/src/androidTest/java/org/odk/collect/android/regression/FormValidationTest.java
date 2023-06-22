package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;

// Issue number NODK-251
@RunWith(AndroidJUnit4.class)
public class FormValidationTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void invalidAnswer_ShouldDisplayAllQuestionsOnOnePage() {
        rule.startAtMainMenu()
                .copyForm("OnePageFormShort.xml")
                .startBlankForm("OnePageFormShort")
                .answerQuestion(0, "A")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickSaveAndExitWithError("Response length must be between 5 and 15")
                .assertText("Integer")
                .answerQuestion(0, "Aaaaa")
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickFinalize();
    }

    @Test
    public void openHierarchyView_ShouldSeeShortForms() {
        //TestCase3
        rule.startAtMainMenu()
                .copyForm("OnePageFormShort.xml")
                .startBlankForm("OnePageFormShort")
                .clickGoToArrow()
                .assertText("YY MM")
                .assertText("YY")
                .pressBack(new FormEntryPage("OnePageFormShort"))
                .closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickDiscardForm();
    }
}
