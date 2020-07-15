package org.odk.collect.android.feature.formmanagement;

import android.Manifest;
import android.webkit.MimeTypeMap;

import androidx.test.rule.GrantPermissionRule;
import androidx.work.WorkManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.NotificationDrawerRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.TestScheduler;
import org.odk.collect.android.support.pages.GetBlankFormPage;
import org.odk.collect.android.support.pages.NotificationDrawer;
import org.odk.collect.async.Scheduler;
import org.odk.collect.utilities.UserAgentProvider;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class PreviouslyDownloadedOnlyTest {

    public TestDependencies testDependencies = new TestDependencies();
    public NotificationDrawerRule notificationDrawer = new NotificationDrawerRule();
    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(notificationDrawer)
            .around(new CopyFormRule("one-question.xml"))
            .around(new CopyFormRule("two-question.xml"))
            .around(rule);

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_notifiesOnFormUpdates_automaticallyAndRepeatedly() {
        rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates();

        testDependencies.server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertNotification("ODK Collect", "Form updates available")
                .clearAll();

        testDependencies.server.addForm("Two Question Updated", "two_question", "two-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertNotification("ODK Collect", "Form updates available");
    }

    @Test // this should probably be tested outside of Espresso instead
    public void whenPreviouslyDownloadedOnlyEnabled_andFormUpdateNotificationHasAlreadyBeenSent_doesntNotifyAgain() {
        rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates();

        testDependencies.server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertNotification("ODK Collect", "Form updates available")
                .clearAll();

        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertNoNotification("ODK Collect");
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_clickingOnNotification_navigatesToGetBlankForm() {
        rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates();

        testDependencies.server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();

        notificationDrawer.open()
                .clickNotification("Collect", "Form updates available", "Get Blank Form", new GetBlankFormPage(rule))
                .assertText(R.string.newer_version_of_a_form_info)
                .assertOnPage();
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_getBlankFormsIsAvailable() {
        rule.mainMenu()
                .enablePreviouslyDownloadedOnlyUpdates()
                .assertText(R.string.get_forms);
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_fillBlankFormRefreshButtonIsGone() {
        rule.mainMenu()
                .enablePreviouslyDownloadedOnlyUpdates()
                .clickFillBlankForm();

        onView(withId(R.id.menu_refresh)).check(doesNotExist());
    }
}
