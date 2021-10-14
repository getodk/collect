package org.odk.collect.android.feature.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.metadata.InstallIDProvider
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage
import org.odk.collect.android.utilities.DeviceDetailsProvider

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
    fun settingMetadata_letsThemBeIncludedInAForm() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .assertPreference(R.string.username, "Chino")
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()
            .assertPreference(R.string.email, "chino@whitepony.com")
            .clickPhoneNumber()
            .inputText("664615")
            .clickOKOnDialog()
            .assertPreference(R.string.phone_number, "664615")
            .pressBack(UserAndDeviceIdentitySettingsPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("Metadata")
            .assertText("Chino", "chino@whitepony.com", "664615")
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
            .clickIgnoreChanges()
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

    @Test
    fun deviceIdentifiersAreDisplayedInSettings() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .assertPreference(R.string.device_id, deviceDetailsProvider.deviceId)
    }

    @Test
    fun deviceIdentifiersCanBeIncludedInAForm() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .startBlankForm("Metadata")
            .scrollToAndAssertText(deviceDetailsProvider.deviceId)
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
            .copyForm("metadata.xml", "second-project.com")
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
            .scrollToAndAssertText("john@second-project.com")
            .scrollToAndAssertText("987654321")
            .scrollToAndAssertText("John Smith")
            .swipeToEndScreen()
            .clickSaveAndExit()

            .openProjectSettingsDialog()
            .selectProject("Demo project")
            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .scrollToAndAssertText("demo@getodk.com")
            .scrollToAndAssertText("123456789")
            .scrollToAndAssertText("Demo user")
            .swipeToEndScreen()
            .clickSaveAndExit()
    }

    private class FakeDeviceDetailsProvider : DeviceDetailsProvider {
        override fun getDeviceId(): String {
            return "deviceID"
        }

        override fun getLine1Number(): String {
            return "line1Number"
        }
    }
}
