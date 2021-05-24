package org.odk.collect.android.feature.formmanagement;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.NotificationDrawerRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.GetBlankFormPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

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
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates();

        testDependencies.server.addForm("One Question Updated", "one_question", "2", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertAndDismissNotification("ODK Collect", "Form updates available");

        testDependencies.server.addForm("Two Question Updated", "two_question", "1", "two-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();
        notificationDrawer.open()
                .assertAndDismissNotification("ODK Collect", "Form updates available");
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_clickingOnNotification_navigatesToGetBlankForm() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates();

        testDependencies.server.addForm("One Question Updated", "one_question", "2", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();

        notificationDrawer.open()
                .clickNotification("Collect", "Form updates available", "Get Blank Form", new GetBlankFormPage())
                .assertText(R.string.newer_version_of_a_form_info)
                .assertOnPage();
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_getBlankFormsIsAvailable() {
        rule.startAtMainMenu()
                .enablePreviouslyDownloadedOnlyUpdates()
                .assertText(R.string.get_forms);
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_fillBlankFormRefreshButtonIsGone() {
        rule.startAtMainMenu()
                .enablePreviouslyDownloadedOnlyUpdates()
                .clickFillBlankForm();

        onView(withId(R.id.menu_refresh)).check(doesNotExist());
    }

    @Test
    public void whenPreviouslyDownloadedOnlyDisabled_stopsCheckingForUpdates() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .enablePreviouslyDownloadedOnlyUpdates()
                .enableManualUpdates();

        assertThat(testDependencies.scheduler.getDeferredTasks(), is(empty()));
    }
}
