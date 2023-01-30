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
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule

class GoogleDriveDeprecationBannerTest {
    val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun bannerIsNotVisibleInNonGoogleDriveProjects() {
        rule
            .startAtMainMenu()
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)
            .assertText(R.string.google_drive_deprecation_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)
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
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)
            .assertTextDoesNotExist(R.string.dismiss_button_text)
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .assertText(R.string.dismiss_button_text)
    }

    @Test
    fun afterClickingDismissTheBannerDisappears() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .clickOnString(R.string.dismiss_button_text)
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
            .rotateToLandscape(MainMenuPage())
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
    }

    @Test
    fun dismissingTheBannerInOneProjectDoesNotAffectOtherProjects() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)
            .clickOnString(R.string.learn_more_button_text)
            .pressBack(MainMenuPage())
            .clickOnString(R.string.dismiss_button_text)
            .assertTextDoesNotExist(R.string.google_drive_deprecation_message)
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount, true)
            .assertText(R.string.google_drive_deprecation_message)
    }
}
