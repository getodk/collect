package org.odk.collect.android.feature.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.ResetStateRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.metadata.DeviceDetailsProvider
import org.odk.collect.metadata.InstallIDProvider

@RunWith(AndroidJUnit4::class)
class FormMetadataSettingsTest {
    private val deviceDetailsProvider: DeviceDetailsProvider = FakeDeviceDetailsProvider()
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(
            ResetStateRule(
                object : AppDependencyModule() {
                    override fun providesDeviceDetailsProvider(
                        context: Context,
                        installIDProvider: InstallIDProvider
                    ): DeviceDetailsProvider {
                        return deviceDetailsProvider
                    }
                }
            )
        )
        .around(rule)

    @Test
    fun metadataShouldBeDisplayedInPreferences() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()

            // First verify that default metadata is displayed
            .assertPreference(R.string.phone_number, deviceDetailsProvider.line1Number)
            .assertPreference(R.string.device_id, deviceDetailsProvider.deviceId)

            // Then set custom metadata
            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("123")
            .clickOKOnDialog()
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()

            // And verify that new metadata is displayed
            .assertPreference(R.string.username, "Chino")
            .assertPreference(R.string.phone_number, "123")
            .assertPreference(R.string.email, "chino@whitepony.com")
    }

    @Test
    fun metadataShouldBeDisplayedInForm() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")

            // First verify that default metadata is displayed
            .startBlankForm("Metadata")
            .assertTexts(deviceDetailsProvider.line1Number, deviceDetailsProvider.deviceId)
            .swipeToEndScreen()
            .pressBack(SaveOrIgnoreDialog("Metadata", MainMenuPage()))
            .clickIgnoreChanges()

            // Then set custom metadata
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("664615")
            .clickOKOnDialog()
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()
            .pressBack(UserAndDeviceIdentitySettingsPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            // And verify that new metadata is displayed
            .startBlankForm("Metadata")
            .assertTexts("Chino", "664615", "chino@whitepony.com", deviceDetailsProvider.deviceId)
    }

    @Test // Issue number NODK-238 TestCase4 TestCase5
    fun settingServerUsername_usedAsFallbackForMetadataUsername() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickServerUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("Metadata")
            .assertText("Chino")
            .pressBack(SaveOrIgnoreDialog("Metadata", MainMenuPage()))
            .clickDiscardForm()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickUsername()
            .inputText("Stephen")
            .clickOKOnDialog()
            .pressBack(UserAndDeviceIdentitySettingsPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("Metadata")
            .assertText("Stephen")
    }

    @Test // https://github.com/getodk/collect/issues/4792
    fun metadataProperties_shouldBeReloadedAfterSwitchingProjects() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickEmail()
            .inputText("demo@getodk.com")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("123456789")
            .clickOKOnDialog()
            .clickUsername()
            .inputText("Demo user")
            .clickOKOnDialog()
            .pressBack(UserAndDeviceIdentitySettingsPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .addAndSwitchToProject("https://second-project.com")
            .copyForm("metadata.xml", projectName = "second-project.com")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickEmail()
            .inputText("john@second-project.com")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("987654321")
            .clickOKOnDialog()
            .clickUsername()
            .inputText("John Smith")
            .clickOKOnDialog()
            .pressBack(UserAndDeviceIdentitySettingsPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .assertText("john@second-project.com")
            .assertText("987654321")
            .assertText("John Smith")
            .swipeToEndScreen()
            .clickSaveAndExit()

            .openProjectSettingsDialog()
            .selectProject("Demo project")
            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .assertText("demo@getodk.com")
            .assertText("123456789")
            .assertText("Demo user")
            .swipeToEndScreen()
            .clickSaveAndExit()
    }

    private class FakeDeviceDetailsProvider : DeviceDetailsProvider {
        override val deviceId: String
            get() = "deviceID"

        override val line1Number: String
            get() = "line1Number"
    }
}
