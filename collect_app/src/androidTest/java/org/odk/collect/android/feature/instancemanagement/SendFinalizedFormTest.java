package org.odk.collect.android.feature.instancemanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.OkDialog;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.SendFinalizedFormPage;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.androidtest.RecordedIntentsRule;
import org.odk.collect.projects.Project;

@RunWith(AndroidJUnit4.class)
public class SendFinalizedFormTest {

    private final TestDependencies testDependencies = new TestDependencies();
    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(new RecordedIntentsRule())
            .around(rule);

    @Test
    public void whenThereIsAnAuthenticationError_allowsUserToReenterCredentials() {
        testDependencies.server.setCredentials("Draymond", "Green");

        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickFinalize()

                .clickSendFinalizedForm(1)
                .clickSelectAll()
                .clickSendSelectedWithAuthenticationError()
                .fillUsername("Draymond")
                .fillPassword("Green")
                .clickOK(new OkDialog())
                .assertText("One Question - Success");
    }

    @Test
    public void canViewSentForms() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickFinalize()

                .clickSendFinalizedForm(1)
                .clickSelectAll()
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .clickViewSentForm(1)
                .clickOnForm("One Question")
                .assertText("123")
                .assertText(R.string.exit);
    }

    @Test
    public void whenDeleteAfterSendIsEnabled_deletesFilledForm() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())

                .openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .scrollToRecyclerViewItemAndClickText(R.string.delete_after_send)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())

                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickFinalize()

                .clickSendFinalizedForm(1)
                .clickSelectAll()
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .clickViewSentForm(1)
                .clickOnText("One Question")
                .assertOnPage();
    }

    @Test
    public void whenGoogleUsedAsServer_sendsSubmissionToSheet() {
        CollectHelpers.addGDProject(
                new Project.New(
                        "GD Project",
                        "G",
                        "#3e9fcc"
                ),
                "dani@davey.com",
                testDependencies
        );

        rule.startAtMainMenu()
                .openProjectSettingsDialog()
                .selectProject("GD Project")
                .copyForm("one-question-google.xml", null, false, "GD Project")
                .startBlankForm("One Question Google")
                .answerQuestion("what is your age", "47")
                .swipeToEndScreen()
                .clickFinalize()

                .clickSendFinalizedForm(1)
                .clickSelectAll()
                .clickSendSelected()
                .assertText("One Question Google - Success");
    }
}
