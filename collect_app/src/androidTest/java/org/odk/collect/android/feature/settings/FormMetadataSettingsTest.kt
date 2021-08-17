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
            .openProjectSettings()
            .clickGeneralSettings()
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
            .openProjectSettings()
            .clickGeneralSettings()
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
            .openProjectSettings()
            .clickGeneralSettings()
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
            .openProjectSettings()
            .clickGeneralSettings()
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

    private class FakeDeviceDetailsProvider : DeviceDetailsProvider {
        override fun getDeviceId(): String {
            return "deviceID"
        }

        override fun getLine1Number(): String {
            return "line1Number"
        }
    }
}
