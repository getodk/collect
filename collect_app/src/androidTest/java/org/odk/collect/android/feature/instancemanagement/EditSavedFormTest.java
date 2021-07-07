package org.odk.collect.android.feature.instancemanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SendFinalizedFormPage;

@RunWith(AndroidJUnit4.class)
public class EditSavedFormTest {
    
    private final CollectTestRule rule = new CollectTestRule();

    final TestDependencies testDependencies = new TestDependencies();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void whenSubmissionSucceeds_instanceNotEditable() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .assertNumberOfEditableForms(0)
                .clickEditSavedForm()
                .assertTextDoesNotExist("One Question")

                // Tests that search doesn't change visibility. Move down to lower testing level.
                // (possibly when replacing CursorLoader)
                .clickMenuFilter()
                .searchInBar("One Question".substring(0, 1))
                .assertTextDoesNotExist("One Question");
    }

    @Test
    public void whenSubmissionFails_instanceNotEditable() {
        testDependencies.server.alwaysReturnError();

        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .assertNumberOfEditableForms(0)
                .clickEditSavedForm()
                .assertTextDoesNotExist("One Question")

                // Tests that search doesn't change visibility. Move down to lower testing level
                // (possibly when replacing CursorLoader)
                .clickMenuFilter()
                .searchInBar("One Question".substring(0, 1))
                .assertTextDoesNotExist("One Question");
    }
}
