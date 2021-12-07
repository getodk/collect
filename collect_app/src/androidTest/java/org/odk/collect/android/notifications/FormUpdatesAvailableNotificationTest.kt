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
import org.odk.collect.android.support.pages.GetBlankFormPage
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.strings.UUIDGenerator
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class FormUpdatesAvailableNotificationTest {
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
    fun testNotification() {
        rule.startAtMainMenu()

        val notifier =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>())
                .notifier()

        val list = listOf(ServerFormDetails("Form 1", "", "1", "1", "", false, true, null))
        notifier.onUpdatesAvailable(list, Project.DEMO_PROJECT_ID)

        val expectedAppName = context.getLocalizedString(R.string.collect_app_name)
        val expectedTitle = context.getLocalizedString(R.string.form_updates_available)

        notificationDrawer
            .open()
            .assertNotification(
                expectedAppName,
                expectedTitle,
                subtext = Project.DEMO_PROJECT_NAME
            )
            .clickNotification(expectedAppName, expectedTitle, expectedTitle, GetBlankFormPage())
    }
}
