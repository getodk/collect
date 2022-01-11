package org.odk.collect.android.feature.formmanagement

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.NotificationDrawerRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.GetBlankFormPage

class PreviouslyDownloadedOnlyTest {
    private val testDependencies = TestDependencies()
    private val notificationDrawerRule = NotificationDrawerRule()
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
        .around(notificationDrawerRule)
        .around(rule)

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_notifiesOnFormUpdates_automaticallyAndRepeatedly() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .copyForm("two-question.xml")
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdates()

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )
        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule.open()
            .assertNotification("ODK Collect", "Form updates available")
            .clearAll()

        testDependencies.server.addForm(
            "Two Question Updated",
            "two_question",
            "1",
            "two-question-updated.xml"
        )
        testDependencies.scheduler.runDeferredTasks()

        notificationDrawerRule.open()
            .assertNotification("ODK Collect", "Form updates available")
            .clickNotification(
                "ODK Collect",
                "Form updates available",
                "Form updates available",
                GetBlankFormPage()
            )
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_getBlankFormsIsAvailable() {
        rule.startAtMainMenu()
            .enablePreviouslyDownloadedOnlyUpdates()
            .assertText(R.string.get_forms)
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_fillBlankFormRefreshButtonIsGone() {
        rule.startAtMainMenu()
            .enablePreviouslyDownloadedOnlyUpdates()
            .clickFillBlankForm()
        onView(withId(R.id.menu_refresh)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun whenPreviouslyDownloadedOnlyDisabled_stopsCheckingForUpdates() {
        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdates()
            .enableManualUpdates()

        assertThat(testDependencies.scheduler.deferredTasks, `is`(empty()))
    }
}
