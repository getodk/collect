package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.StubBarcodeViewDecoder
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.views.BarcodeViewDecoder

class AddNewProjectTest {

    val rule = CollectTestRule()

    private val stubBarcodeViewDecoder = StubBarcodeViewDecoder()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(
            ResetStateRule(object : AppDependencyModule() {
                override fun providesBarcodeViewDecoder(): BarcodeViewDecoder {
                    return stubBarcodeViewDecoder
                }
            })
        )
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
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("my-server.com", "John / my-server.com")
    }

    @Test
    fun addingProjectAutomatically_addsNewProject() {
        val page = rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()

        stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(R.string.new_project_created)

        MainMenuPage()
            .assertOnPage()
            .openProjectSettings()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("my-server.com", "adam / my-server.com")
    }
}
