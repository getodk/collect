package org.odk.collect.android.feature.formentry.audit

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.StorageUtils
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AuditTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun fillAndEditingForm_updatesAuditLogForInstance() {
        rule.startAtMainMenu()
            .copyForm("one-question-audit.xml")
            .startBlankForm("One Question Audit")
            .fillOut(
                FormEntryPage.QuestionAndAnswer("what is your age", "31")
            )
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickEditSavedForm(1)
            .clickOnForm("One Question Audit")
            .clickGoToStart()
            .fillOutAndFinalize(
                FormEntryPage.QuestionAndAnswer("what is your age", "32")
            )

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog.size, equalTo(12))

        assertThat(auditLog[0].get("event"), equalTo("form start"))
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[2].get("event"), equalTo("end screen"))
        assertThat(auditLog[3].get("event"), equalTo("form save"))
        assertThat(auditLog[4].get("event"), equalTo("form exit"))

        assertThat(auditLog[5].get("event"), equalTo("form resume"))
        assertThat(auditLog[6].get("event"), equalTo("jump"))
        assertThat(auditLog[7].get("event"), equalTo("question"))
        assertThat(auditLog[8].get("event"), equalTo("end screen"))
        assertThat(auditLog[9].get("event"), equalTo("form save"))
        assertThat(auditLog[10].get("event"), equalTo("form exit"))
        assertThat(auditLog[11].get("event"), equalTo("form finalize"))
    }

    @Test // https://github.com/getodk/collect/issues/5551
    fun navigatingToSettings_savesAnswersFormCurrentScreenToAuditLog() {
        rule.startAtMainMenu()
            .copyForm("one-question-audit-track-changes.xml")
            .startBlankForm("One Question Audit Track Changes")
            .fillOut(
                FormEntryPage.QuestionAndAnswer("What is your age", "31")
            )
            .clickOptionsIcon()
            .clickGeneralSettings()

        val auditLog = StorageUtils.getAuditLogForFirstInstance()
        assertThat(auditLog[1].get("event"), equalTo("question"))
        assertThat(auditLog[1].get("new-value"), equalTo("31"))
    }
}
