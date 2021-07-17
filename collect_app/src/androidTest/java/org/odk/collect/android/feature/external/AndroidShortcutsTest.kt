package org.odk.collect.android.feature.external

import android.app.Application
import android.content.Intent
import android.content.Intent.EXTRA_SHORTCUT_INTENT
import android.content.Intent.EXTRA_SHORTCUT_NAME
import android.provider.BaseColumns
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsProviderAPI
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage

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
            .copyForm("two-question.xml")
            .clickFillBlankForm() // Load form

        rule.launchShortcuts()
            .assertText("Two Question")
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun shortcutIsFormViewAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Load form

        val shortcutIntent = rule.launchShortcuts()
            .selectForm("One Question")
        assertThat(shortcutIntent.getStringExtra(EXTRA_SHORTCUT_NAME), equalTo("One Question"))

        val shortcutTargetIntent =
            shortcutIntent.getParcelableExtra<Intent>(EXTRA_SHORTCUT_INTENT)!!
        val formId = getFirstFormIdFromContentProvider("DEMO")
        assertThat(shortcutTargetIntent.action, equalTo(Intent.ACTION_VIEW))
        assertThat(shortcutTargetIntent.data, equalTo(FormsProviderAPI.getUri("DEMO", formId)))
    }

    private fun getFirstFormIdFromContentProvider(projectId: String): Long {
        val contentResolver =
            ApplicationProvider.getApplicationContext<Application>().contentResolver
        val uri = FormsProviderAPI.getUri(projectId)
        return contentResolver.query(uri, null, null, null, null, null).use {
            if (it != null) {
                it.moveToFirst()
                it.getLong(it.getColumnIndex(BaseColumns._ID))
            } else {
                throw RuntimeException("Null cursor!")
            }
        }
    }
}
