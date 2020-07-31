package org.odk.collect.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class DeleteBlankFormTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(new CopyFormRule("one-question.xml"))
            .around(rule);

    @Test
    public void deletingAForm_removesFormFromBlankFormList() {
        rule.mainMenu()
                .clickDeleteSavedForm()
                .clickBlankForms()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .pressBack(new MainMenuPage(rule))
                .clickFillBlankForm()
                .assertTextDoesNotExist("One Question");
    }

    @Test
    public void deletingAForm_whenThereFilledForms_allowsEditing() {
        rule.mainMenu()
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "22")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickDeleteSavedForm()
                .clickBlankForms()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .assertTextDoesNotExist("One Question")
                .pressBack(new MainMenuPage(rule))

                .clickEditSavedForm()
                .clickOnForm("One Question")
                .clickOnQuestion("what is your age")
                .answerQuestion("what is your age", "30")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
