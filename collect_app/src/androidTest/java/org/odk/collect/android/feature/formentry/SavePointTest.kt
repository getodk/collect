package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog
import org.odk.collect.android.support.rules.FormActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class SavePointTest {

    private val rule = FormActivityTestRule("two-question-audit.xml", "Two Question")

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun savePointIsCreateWhenMovingForwardInForm() {
        rule.startInFormEntry()
            .answerQuestion("What is your name?", "Alexei")
            .swipeToNextQuestion("What is your age?")
            .answerQuestion("What is your age?", "46")
            .let { simulateBatteryDeath() }

            .startInFormHierarchy()
            .assertText("Alexei")
            .assertTextDoesNotExist("46")
            .pressBack(FormEntryPage("Two Question"))
            .assertQuestion("What is your name?")
            .pressBack(SaveOrIgnoreDialog("Two Question", AppClosedPage()))
            .clickSaveChanges()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(7))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        // Second question event not logged - possibly a problem

        assertThat(auditLog[2].get("event"), equalTo("form resume"))
        assertThat(auditLog[3].get("event"), equalTo("jump"))
        assertThat(auditLog[4].get("event"), equalTo("question"))
        assertThat(auditLog[5].get("event"), equalTo("form save"))
        assertThat(auditLog[6].get("event"), equalTo("form exit"))
    }

    @Test
    fun savePointIsCreatedWhenLeavingTheApp() {
        rule.startInFormEntry()
            .answerQuestion("What is your name?", "Alexei")
            .let { simulateProcessRestore() }

            .startInFormHierarchy()
            .assertText("Alexei")
            .pressBack(FormEntryPage("Two Question"))
            .assertQuestion("What is your name?")
            .pressBack(SaveOrIgnoreDialog("Two Question", AppClosedPage()))
            .clickSaveChanges()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(6))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        // Question event not logged - possibly a problem

        assertThat(auditLog[1].get("event"), equalTo("form resume"))
        assertThat(auditLog[2].get("event"), equalTo("jump"))
        assertThat(auditLog[3].get("event"), equalTo("question"))
        assertThat(auditLog[4].get("event"), equalTo("form save"))
        assertThat(auditLog[5].get("event"), equalTo("form exit"))
    }

    /**
     * Simulates a case where the process is killed without lifecycle clean up (like a phone
     * being battery dying).
     */
    private fun simulateBatteryDeath(): FormActivityTestRule {
        return rule.reset()
    }

    /**
     * Simulate a "process restore" case where an app in the background is killed by Android
     * to reclaim memory, change permissions etc
     */
    private fun simulateProcessRestore(): FormActivityTestRule {
        return rule.saveInstanceStateForActivity()
            .destroyActivity()
            .reset()
    }
}
