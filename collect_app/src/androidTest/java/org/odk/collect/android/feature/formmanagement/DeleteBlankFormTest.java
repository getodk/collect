package org.odk.collect.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class DeleteBlankFormTest {

    public final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void deletingAForm_removesFormFromBlankFormList() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickDeleteSavedForm()
                .clickBlankForms()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .assertTextDoesNotExist("One Question")
                .pressBack(new MainMenuPage())
                .clickFillBlankForm()
                .assertNoForms();
    }

    @Test
    public void deletingAForm_whenThereFilledForms_removesFormFromBlankFormList_butAllowsEditingFilledForms() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "22")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickDeleteSavedForm()
                .clickBlankForms()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .pressBack(new MainMenuPage())
                .clickFillBlankForm()
                .assertNoForms()
                .pressBack(new MainMenuPage())

                .clickEditSavedForm()
                .clickOnForm("One Question")
                .clickOnQuestion("what is your age")
                .answerQuestion("what is your age", "30")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void afterFillingAForm_andDeletingIt_allowsFormToBeReDownloaded() {
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml");

        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .clickGetBlankForm()
                .clickGetSelected()
                .assertText("One Question (Version:: 1 ID: one_question) - Success")
                .clickOK(new MainMenuPage())
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "22")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickDeleteSavedForm()
                .clickBlankForms()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .pressBack(new MainMenuPage())

                .clickGetBlankForm()
                .clickGetSelected()
                .assertText("One Question (Version:: 1 ID: one_question) - Success")
                .clickOK(new MainMenuPage())
                .clickFillBlankForm()
                .assertFormExists("One Question");
    }
}
