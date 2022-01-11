package org.odk.collect.android.feature.settings

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.AdbFormLoadingUtils
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.NotificationDrawerRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.FillBlankFormPage
import org.odk.collect.android.support.pages.FormsDownloadErrorPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.testshared.RecordedIntentsRule

@RunWith(AndroidJUnit4::class)
class FormManagementSettingsTest {
    private val testDependencies = TestDependencies()
    private val notificationDrawer = NotificationDrawerRule()
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
        .around(RecordedIntentsRule())
        .around(notificationDrawer)
        .around(rule)

    @Test
    fun whenMatchExactlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks, `is`(empty()))

        val page = MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(R.string.match_exactly)

        deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks.size, `is`(1))

        val matchExactlyTag = deferredTasks[0].tag

        page.clickAutomaticUpdateFrequency().clickOption(R.string.every_one_hour)
        deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(matchExactlyTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks, `is`(empty()))

        val page = MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(R.string.previously_downloaded_only)

        deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks.size, `is`(1))

        val previouslyDownloadedTag = deferredTasks[0].tag
        page.clickAutomaticUpdateFrequency().clickOption(R.string.every_one_hour)

        deferredTasks = testDependencies.scheduler.deferredTasks

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(previouslyDownloadedTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotification() {
        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(R.string.previously_downloaded_only)
            .clickOnString(R.string.automatic_download)

        AdbFormLoadingUtils.copyFormToDemoProject("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .clickFillBlankForm()
            .assertText("One Question Updated")

        notificationDrawer.open()
            .assertNotification(
                "ODK Collect",
                "Forms download succeeded",
                "All downloads succeeded!"
            )
            .clickNotification("ODK Collect", "Forms download succeeded", "All downloads succeeded!", FillBlankFormPage())
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_checkingAutoDownload_downloadsUpdatedForms_andDisplaysNotificationWhenFails() {
        testDependencies.server.errorOnFetchingForms()

        val page = MainMenuPage().assertOnPage()
            .setServer(testDependencies.server.url)
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(R.string.previously_downloaded_only)
            .clickOnString(R.string.automatic_download)

        AdbFormLoadingUtils.copyFormToDemoProject("one-question.xml")

        testDependencies.server.addForm(
            "One Question Updated",
            "one_question",
            "2",
            "one-question-updated.xml"
        )

        testDependencies.scheduler.runDeferredTasks()

        page.pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .clickFillBlankForm()
            .assertFormDoesNotExist("One Question Updated")

        notificationDrawer.open()
            .assertNotification(
                "ODK Collect",
                "Forms download failed",
                "1 of 1 downloads failed!"
            )
            .clickNotification("ODK Collect", "Forms download failed", "1 of 1 downloads failed!", FormsDownloadErrorPage())
    }

    @Test
    fun whenGoogleDriveUsingAsServer_disablesPrefsAndOnlyAllowsManualUpdates() {
        testDependencies.googleAccountPicker.setDeviceAccount("steph@curry.basket")

        MainMenuPage().assertOnPage()
            .enablePreviouslyDownloadedOnlyUpdates() // Enabled a different mode before setting up Google
            .setGoogleAccount("steph@curry.basket")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .assertDisabled(R.string.form_update_mode_title)
            .assertDisabled(R.string.form_update_frequency_title)
            .assertDisabled(R.string.automatic_download)
            .assertText(R.string.manual)

        assertThat(testDependencies.scheduler.deferredTasks.size, `is`(0))
    }
}
