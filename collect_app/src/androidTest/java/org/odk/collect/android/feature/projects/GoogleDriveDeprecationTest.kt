package org.odk.collect.android.feature.projects

import android.app.Application
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.projects.Project

class GoogleDriveDeprecationTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun bannerIsNotVisibleInNonGoogleDriveProjects() {
        rule
            .startAtMainMenu()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        addProject(Project.Saved("1", "Old GD project", "A", "#ffffff", true))

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .assertText(org.odk.collect.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        addProject(Project.Saved("1", "Old GD project", "A", "#ffffff", true))

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .clickOnString(org.odk.collect.strings.R.string.learn_more_button_text)

        assertThat(
            testDependencies.webPageService.openedPages,
            equalTo(listOf("https://forum.getodk.org/t/40097".toUri()))
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
