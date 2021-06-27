package org.odk.collect.android.feature.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.StubBarcodeViewDecoder
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.views.BarcodeViewDecoder

@RunWith(AndroidJUnit4::class)
class LaunchScreenTest {

    private val rule = CollectTestRule(false)

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
    fun clickingTryCollectAtLaunch_setsAppUpWithDemoProject() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettings()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .clickGeneralSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
    }

    @Test
    fun clickingManuallyEnterProjectDetails_andAddingProjectDetails_setsAppUpWithProjectDetails() {
        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()
            .assertProjectIcon("M")
            .openProjectSettings()
            .assertCurrentProject("my-server.com", "John / my-server.com")
    }

    @Test
    fun clickingAutomaticallyEnterProjectDetails_andScanningQRCode_setsAppUpWithProjectDetails() {
        val page = rule.startAtFirstLaunch()
            .clickConfigureWithQrCode()

        stubBarcodeViewDecoder.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(R.string.switched_project, "my-server.com")

        MainMenuPage()
            .assertOnPage()
            .openProjectSettings()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
    }
}
