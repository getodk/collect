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
import org.odk.collect.android.support.AdbFormLoadingUtils
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FillBlankFormPage
import org.odk.collect.android.support.pages.FormsDownloadErrorPage
import org.odk.collect.android.support.pages.GetBlankFormPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.NotificationDrawerRule
import org.odk.collect.android.support.rules.TestRuleChain

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
    fun whenPreviouslyDownloadedOnlyEnabledWithAutomaticDownload_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotification() {
        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload()

        AdbFormLoadingUtils.copyFormToDemoProject("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.clickFillBlankForm()
            .assertText("One Question Updated")

        notificationDrawerRule.open()
            .assertNotification(
                "ODK Collect",
                "Forms download succeeded",
                "All downloads succeeded!"
            )
            .clickNotification(
                "ODK Collect",
                "Forms download succeeded",
                "All downloads succeeded!",
                FillBlankFormPage()
            )
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabledWithAutomaticDownload_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotificationWhenFails() {
        testDependencies.server.errorOnFetchingForms()

        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload()

        AdbFormLoadingUtils.copyFormToDemoProject("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.clickFillBlankForm()
            .assertFormDoesNotExist("One Question Updated")

        notificationDrawerRule.open()
            .assertNotification(
                "ODK Collect",
                "Forms download failed",
                "1 of 1 downloads failed!"
            )
            .clickNotification(
                "ODK Collect",
                "Forms download failed",
                "1 of 1 downloads failed!",
                FormsDownloadErrorPage()
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
