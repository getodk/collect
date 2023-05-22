package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.rules.FormEntryActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class ProcessRestoreTest {

    private val rule = FormEntryActivityTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun whenProcessIsKilledAndRestoredDuringFormEntry_returnsToHierarchy() {
        // Create save point
        rule.setUpProjectAndCopyForm("one-question.xml")
            .fillNewForm("one-question.xml", "One Question")
            .answerQuestion("what is your age", "123")
            .let { simulateProcessRestore(FormHierarchyPage("One Question")) }
            .assertText("123")
            .pressBack(FormEntryPage("One Question"))
            .assertQuestion("what is your age")
    }

    /**
     * Simulate a "process restore" case where an app in the background is killed by Android
     * to reclaim memory, change permissions etc and then the process is recreated (backstack etc)
     * when navigated back to
     */
    private fun <T : Page<T>> simulateProcessRestore(destination: Page<T>): Page<T> {
        rule.recreateActivity {
            CollectHelpers.simulateProcessRestart()
        }

        return destination.assertOnPage()
    }
}
