package org.odk.collect.android.feature.projects

import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.projects.Project

class GoogleDriveDeprecationBannerTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    private val gdProject1 = Project.Saved(
        "1",
        "GD Project 1",
        "G",
        "#3e9fcc"
    )

    private val gdProject2 = Project.Saved(
        "2",
        "GD Project 2",
        "G",
        "#3e9fcc"
    )

    @Test
    fun bannerIsNotVisibleInNonGoogleDriveProjects() {
        rule
            .startAtMainMenu()
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("GD Project 1")
            .assertText(R.string.google_drive_deprecation_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("GD Project 1")
            .clickOnString(R.string.learn_more_button_text)

        intended(
            allOf(
                hasComponent(WebViewActivity::class.java.name),
                hasExtra("url", "https://forum.getodk.org/t/40097")
            )
        )
    }

    @Test
    fun dismissButtonIsVisibleOnlyAfterClickingLearnMore() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("GD Project 1")
            .assertTextDoesNotExist(R.string.dismiss_button_text)
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .assertText(R.string.dismiss_button_text)
    }

    @Test
    fun afterClickingDismissTheBannerDisappears() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("GD Project 1")
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .clickOnString(R.string.dismiss_button_text)
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
            .rotateToLandscape(MainMenuPage())
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
    }

    @Test
    fun dismissingTheBannerInOneProjectDoesNotAffectOtherProjects() {
        CollectHelpers.addGDProject(gdProject1, "steph@curry.basket", testDependencies)
        CollectHelpers.addGDProject(gdProject2, "john@curry.basket", testDependencies)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("GD Project 1")
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .clickOnString(R.string.dismiss_button_text)
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
            .openProjectSettingsDialog()
            .selectProject("GD Project 2")
            .assertText(R.string.google_drive_deprecation_message)
    }
}
