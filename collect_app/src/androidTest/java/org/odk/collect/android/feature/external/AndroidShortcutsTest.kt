package org.odk.collect.android.feature.external

import android.content.Intent
import android.content.Intent.EXTRA_SHORTCUT_INTENT
import android.content.Intent.EXTRA_SHORTCUT_NAME
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AndroidShortcutsTest {

    private var rule = CollectTestRule()

    @get:Rule
    var testRuleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun showsFormsForCurrentProject() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Load form
            .pressBack(MainMenuPage())
            .addAndSwitchToProject("https://example.com")
            .copyForm("two-question.xml", projectName = "example.com")
            .clickFillBlankForm() // Load form

        rule.launchShortcuts()
            .assertText("Two Question")
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun shortcutIsFormEditAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Load form

        val shortcutIntent = rule.launchShortcuts()
            .selectForm("One Question")
        assertThat(shortcutIntent.getStringExtra(EXTRA_SHORTCUT_NAME), equalTo("One Question"))

        val shortcutTargetIntent =
            shortcutIntent.getParcelableExtra<Intent>(EXTRA_SHORTCUT_INTENT)!!
        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        assertThat(shortcutTargetIntent.action, equalTo(Intent.ACTION_EDIT))
        assertThat(shortcutTargetIntent.data, equalTo(FormsContract.getUri("DEMO", formId)))
    }
}
