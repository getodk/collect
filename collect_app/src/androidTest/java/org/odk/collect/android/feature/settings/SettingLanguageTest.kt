package org.odk.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class SettingLanguageTest {
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun settingLanguageWithoutCountryCodeShouldLoadProperTranslations() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickOnLanguage()
            .clickOnSelectedLanguage("dansk")
            .openProjectSettingsDialog()
            .clickSettings()
            .assertText("Kort")
    }

    @Test
    fun settingLanguageWithCountryCodeShouldLoadProperTranslations() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickOnLanguage()
            .clickOnSelectedLanguage("svenska (Sverige)")
            .assertText("Börja nytt formulär")
    }
}
