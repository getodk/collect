package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule

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
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "John / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun addingGdriveProjectManually_addsNewProject() {
        val googleAccount = "steph@curry.basket"
        testDependencies.googleAccountPicker.setDeviceAccount(googleAccount)

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .openGooglePickerAndSelect(googleAccount)

            .openProjectSettingsDialog()
            .assertCurrentProject(googleAccount, "$googleAccount / Google Drive")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun addingProjectFromQrCode_addsNewProject() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(R.string.switched_project, "my-server.com")

        MainMenuPage()
            .assertOnPage()
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun switchesToExistingProject_whenDuplicateProjectScanned_andOptionToSwitchToExistingSelected() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https://demo.getodk.org\"},\"admin\":{}}")

        page.assertDuplicateDialogShown()
            .switchToExistingProject()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertNotInactiveProject("Demo project")
    }

    @Test
    fun addsDuplicateProject_whenDuplicateProjectScanned_andOptionToAddDuplicateProjectSelected() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https://demo.getodk.org\"},\"admin\":{}}")

        page.assertDuplicateDialogShown()
            .addDuplicateProject()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun switchesToExistingProject_whenDuplicateProjectEnteredManually_andOptionToSwitchToExistingSelected() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://demo.getodk.org")
            .addProjectAndAssertDuplicateDialogShown()
            .switchToExistingProject()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertNotInactiveProject("Demo project")
    }

    @Test
    fun addsDuplicateProject_whenDuplicateProjectEnteredManually_andOptionToAddDuplicateProjectSelected() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://demo.getodk.org")
            .addProjectAndAssertDuplicateDialogShown()
            .addDuplicateProject()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }
}
