package org.odk.collect.android.feature.smoke;

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
public class GetAndSubmitFormTest {

    private final CollectTestRule rule = new CollectTestRule(false);
    private final TestDependencies testDependencies = new TestDependencies();

    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies).around(rule);

    @Test
    public void canGetBlankForm_fillItIn_andSubmit() {
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml");

        rule.withProject(testDependencies.server.getURL())
                // Fetch form
                .clickGetBlankForm()
                .clickGetSelected()
                .assertMessage("All downloads succeeded!")
                .clickOKOnDialog(new MainMenuPage())

                // Fill out form
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()

                // Send form
                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .assertText("One Question - Success")
                .clickOK(new SendFinalizedFormPage())
                .assertTextDoesNotExist("One Question")

                // Back to the start
                .pressBack(new MainMenuPage())
                .assertNumberOfFinalizedForms(0);
    }
}
