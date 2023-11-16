package org.odk.collect.android.feature.projects

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.MetaKeys

class GoogleDriveDeprecationTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    private val gdProject1 = Project.New(
        "GD Project 1",
        "G",
        "#3e9fcc"
    )

    private val gdProject2 = Project.New(
        "GD Project 2",
        "G",
        "#3e9fcc"
    )

    @Test
    fun bannerIsNotVisibleInNonGoogleDriveProjects() {
        rule
            .startAtMainMenu()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        val newProject = addProject(Project.New("Old GD project", "A", "#ffffff"))
        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        component
            .settingsProvider()
            .getMetaSettings()
            .save(MetaKeys.getKeyIsOldGDProject(newProject.uuid), true)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .assertText(org.odk.collect.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        val newProject = addProject(Project.New("Old GD project", "A", "#ffffff"))
        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        component
            .settingsProvider()
            .getMetaSettings()
            .save(MetaKeys.getKeyIsOldGDProject(newProject.uuid), true)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .clickOnString(org.odk.collect.strings.R.string.learn_more_button_text)

        intended(
            allOf(
                hasComponent(WebViewActivity::class.java.name),
                hasExtra("url", "https://forum.getodk.org/t/40097")
            )
        )
    }

    @Test
    fun reconfiguringShouldBeVisibleInNonGoogleDriveProjects() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .assertText(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title)
    }

    private fun addProject(project: Project): Project.Saved {
        val component =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        return component.projectsRepository().save(project)
    }
}
