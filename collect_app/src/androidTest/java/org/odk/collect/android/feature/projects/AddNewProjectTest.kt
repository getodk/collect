package org.odk.collect.android.feature.projects

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.RecordedIntentsRule
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage

class AddNewProjectTest {

    val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun addingProjectManually_addsNewProject() {
        rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            .openProjectSettings()
            .assertCurrentProject("my-server.com", "John / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun addingGdriveProjectManually_addsNewProject() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()
            .switchToManualMode()
            .setGoogleAccount(googleAccount)

            .openProjectSettings()
            .assertCurrentProject(googleAccount, "$googleAccount / Google Drive")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun addingProjectAutomatically_addsNewProject() {
        val page = rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()

        testDependencies.stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(R.string.switched_project, "my-server.com")

        MainMenuPage()
            .assertOnPage()
            .openProjectSettings()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }
}
