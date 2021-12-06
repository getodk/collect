package org.odk.collect.android.notifications

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.NotificationDrawerRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.FillBlankFormPage
import org.odk.collect.android.support.pages.FormsDownloadErrorPage
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.strings.UUIDGenerator
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class FormUpdatesDownloadedNotificationTest {
    private val rule = CollectTestRule()
    private val notificationDrawer = NotificationDrawerRule()

    private val testDependencies: TestDependencies = object : TestDependencies() {
        override fun providesProjectsRepository(
            uuidGenerator: UUIDGenerator,
            gson: Gson,
            settingsProvider: SettingsProvider
        ): ProjectsRepository {
            return InMemProjectsRepository().apply {
                save(Project.DEMO_PROJECT)
            }
        }
    }

    @get:Rule
    var ruleChain = TestRuleChain.chain(testDependencies)
        .around(ResetStateRule())
        .around(notificationDrawer)
        .around(rule)

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun testNotificationWithNoErrors() {
        rule.startAtMainMenu()

        displaySuccessNotification()

        val expectedAppName = context.getLocalizedString(R.string.collect_app_name)
        val expectedTitle = context.getLocalizedString(R.string.forms_download_succeeded)
        val expectedText = context.getLocalizedString(R.string.all_downloads_succeeded)

        notificationDrawer
            .open()
            .assertNotification(expectedAppName, expectedTitle, expectedText, Project.DEMO_PROJECT_NAME)
            .clickNotification(expectedAppName, expectedTitle, expectedTitle, FillBlankFormPage())
    }

    @Test
    fun testNotificationWithErrors() {
        rule.startAtMainMenu()

        displayErrorNotification()

        val expectedAppName = context.getLocalizedString(R.string.collect_app_name)
        val expectedTitle = context.getLocalizedString(R.string.forms_download_failed)
        val expectedText = context.getLocalizedString(R.string.some_downloads_failed, 1, 1)

        notificationDrawer
            .open()
            .assertNotification(expectedAppName, expectedTitle, expectedText, Project.DEMO_PROJECT_NAME)
            .clickNotification(expectedAppName, expectedTitle, expectedTitle, FormsDownloadErrorPage())
    }

    private fun displaySuccessNotification() {
        val formDetails1 = ServerFormDetails("Form 1", "", "1", "1", "", false, true, null)
        val resultWithoutErrors = mapOf(
            formDetails1 to context.getString(R.string.success),
        )

        val notifier = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>())
            .notifier()

        notifier.onUpdatesDownloaded(resultWithoutErrors, Project.DEMO_PROJECT_ID)
    }

    private fun displayErrorNotification() {
        val formDetails1 = ServerFormDetails("Form 1", "", "1", "1", "", false, true, null)
        val resultWithoutErrors = mapOf(
            formDetails1 to "Some exception",
        )

        val notifier = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>())
            .notifier()

        notifier.onUpdatesDownloaded(resultWithoutErrors, Project.DEMO_PROJECT_ID)
    }
}
