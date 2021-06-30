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

    public static final String TEXT_FORM_NAME = "One Question";
    public static final String TEXT_FORM_XML = "one-question.xml";
    public static final String TEXT_QUESTION = "what is your age";
    public static final String TEXT_ANSWER = "123";

    private final CollectTestRule rule = new CollectTestRule();

    final TestDependencies testDependencies = new TestDependencies();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void whenSubmissionSucceeds_instanceNotEditable() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .copyForm(TEXT_FORM_XML)
                .startBlankForm(TEXT_FORM_NAME)
                .answerQuestion(TEXT_QUESTION, TEXT_ANSWER)
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm(TEXT_FORM_NAME)
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .assertNumberOfEditableForms(0)
                .clickEditSavedForm()
                .assertTextDoesNotExist(TEXT_FORM_NAME);
    }

    @Test
    public void whenSubmissionFails_instanceNotEditable() {
        testDependencies.server.alwaysReturnError();

        rule.startAtMainMenu()
                .copyForm(TEXT_FORM_XML)
                .startBlankForm(TEXT_FORM_NAME)
                .answerQuestion(TEXT_QUESTION, TEXT_ANSWER)
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm(TEXT_FORM_NAME)
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .assertNumberOfEditableForms(0)
                .clickEditSavedForm()
                .assertTextDoesNotExist(TEXT_FORM_NAME);
    }
}
