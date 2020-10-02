package org.odk.collect.android.feature.instancemanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.NotificationDrawerRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class AutoSendTest {

    public CollectTestRule rule = new CollectTestRule();

    final TestDependencies testDependencies = new TestDependencies();
    final NotificationDrawerRule notificationDrawerRule = new NotificationDrawerRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(notificationDrawerRule)
            .around(new CopyFormRule("one-question.xml"))
            .around(rule);

    @Test
    public void whenAutoSendEnabled_fillingAndFinalizingForm_sendsFormAndNotifiesUser() {
        MainMenuPage mainMenuPage = rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .enableAutoSend()
                .clickFillBlankForm()
                .clickOnForm("One Question")
                .inputText("The greatest answer")
                .swipeToEndScreen()
                .clickSaveAndExit();

        testDependencies.scheduler.runDeferredTasks();

        mainMenuPage
                .clickViewSentForm(1)
                .assertText("One Question");

        notificationDrawerRule.open()
                .assertAndDismissNotification("ODK Collect", "ODK auto-send results", "Success");
    }
}
