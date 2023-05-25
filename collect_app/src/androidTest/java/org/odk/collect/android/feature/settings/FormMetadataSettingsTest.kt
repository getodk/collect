package org.odk.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.ResetStateRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.metadata.InstallIDProvider
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class FormMetadataSettingsTest {
    private val installIDProvider = FakeInstallIDProvider()
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(
            ResetStateRule(
                object : AppDependencyModule() {
                    override fun providesInstallIDProvider(settingsProvider: SettingsProvider): InstallIDProvider {
                        return installIDProvider
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

            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("123")
            .clickOKOnDialog()
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()

            .assertPreference(R.string.username, "Chino")
            .assertPreference(R.string.phone_number, "123")
            .assertPreference(R.string.email, "chino@whitepony.com")
            .assertPreference(R.string.device_id, installIDProvider.installID)
    }

    @Test
    fun metadataShouldBeDisplayedInForm() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")

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
            .assertTexts("Chino", "664615", "chino@whitepony.com", installIDProvider.installID)
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
            .pressBack(SaveOrDiscardFormDialog(MainMenuPage()))
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
            .assertTexts("john@second-project.com", "987654321", "John Smith")
            .swipeToEndScreen()
            .clickFinalize()

            .openProjectSettingsDialog()
            .selectProject("Demo project")
            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .assertTexts("demo@getodk.com", "123456789", "Demo user")
            .swipeToEndScreen()
            .clickFinalize()
    }

    private class FakeInstallIDProvider : InstallIDProvider {
        override val installID: String
            get() = "deviceID"
    }
}
