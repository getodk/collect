package org.odk.collect.android.feature.projects

import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.OkDialog
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
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.google_drive_deprecation_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject(gdProject1.name)
            .assertText(org.odk.collect.strings.R.string.google_drive_deprecation_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject(gdProject1.name)
            .clickOnString(org.odk.collect.strings.R.string.learn_more_button_text)

        intended(
            allOf(
                hasComponent(WebViewActivity::class.java.name),
                hasExtra("url", "https://forum.getodk.org/t/40097")
            )
        )
    }

    @Test
    fun additionalWarningShouldNotBeDisplayedWhenRemovingNonGDProject() {
        rule
            .startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnDeleteProject()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.delete_google_drive_project_confirm_message)
    }

    @Test
    fun additionalWarningShouldBeDisplayedWhenRemovingGDProject() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule
            .startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject(gdProject1.name)
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnDeleteProject()
            .assertText(org.odk.collect.strings.R.string.delete_google_drive_project_confirm_message)
    }

    @Test
    fun reconfiguringShouldBeVisibleInNonGoogleDriveProjects() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .assertText(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title)
    }

    @Test
    fun reconfiguringShouldBeHiddenInGoogleDriveProjects() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject(gdProject1.name)
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.reconfigure_with_qr_code_settings_title)
    }

    @Test
    fun warningIsShownWhenTryingToDownloadForms() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject(gdProject1.name)
            .clickGetBlankForm(OkDialog())
            .assertText(org.odk.collect.strings.R.string.cannot_start_new_forms_in_google_drive_projects)
            .clickOK(MainMenuPage())
    }
}
